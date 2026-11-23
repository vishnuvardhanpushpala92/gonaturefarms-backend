package com.gonaturefarms.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.order.OrderItemRequest;
import com.gonaturefarms.dto.order.OrderRequest;
import com.gonaturefarms.entity.Coupon;
import com.gonaturefarms.entity.Order;
import com.gonaturefarms.entity.OrderItem;
import com.gonaturefarms.entity.Product;
import com.gonaturefarms.entity.ProductVariant;
import com.gonaturefarms.entity.SiteSetting;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.exception.ResourceNotFoundException;
import com.gonaturefarms.repository.CouponRepository;
import com.gonaturefarms.repository.DeliveryZoneRepository;
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.repository.SiteSettingRepository;
import com.gonaturefarms.util.OrderIdGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Free direct-UPI checkout flow.
 *
 * The customer pays straight to the merchant's own UPI ID. No payment gateway
 * is involved, so payments CANNOT be verified automatically — the order is
 * marked PAYMENT VERIFICATION PENDING after the customer confirms, and only an
 * admin (who can see the money arrive in the merchant UPI/bank app) can mark
 * it PAID.
 *
 * All amounts are recomputed server-side from PostgreSQL product/variant
 * prices, delivery zones, coupons and site settings — client-sent totals are
 * never trusted.
 */
@Service
public class UpiPaymentService {

    private static final String GATEWAY = "direct_upi";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final CouponRepository couponRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final UpiQrService upiQrService;

    public UpiPaymentService(OrderRepository orderRepository,
                             ProductRepository productRepository,
                             DeliveryZoneRepository deliveryZoneRepository,
                             CouponRepository couponRepository,
                             SiteSettingRepository siteSettingRepository,
                             UpiQrService upiQrService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.couponRepository = couponRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.upiQrService = upiQrService;
    }

    /** Public UPI details for the payment screen (UPI ID is not a secret — it is printed on the QR anyway). */
    @Transactional(readOnly = true)
    public ApiResponse upiConfig() {
        boolean enabled = upiEnabled();
        String id = upiId();
        boolean valid = isValidUpiId(id);
        return ApiResponse.ok()
                .with("upi_enabled", enabled)
                .with("upi_id", id)
                .with("payee_name", payeeName())
                .with("qr_image_url", upiQrService.activeImageUrl())
                .with("payment_instructions", setting("payment_instructions"))
                .with("configured", enabled && valid)
                .with("config_error", configError(enabled, valid));
    }

    /**
     * Creates a pending order with server-verified totals and returns the
     * order-specific UPI payment link + QR code (as a data URL).
     */
    @Transactional
    public ApiResponse createCheckout(OrderRequest req) {
        // ── Idempotency: never create a second order for the same checkout attempt ──
        if (!isBlank(req.getIdempotencyKey())) {
            var existing = orderRepository.findByIdempotencyKey(req.getIdempotencyKey().trim());
            if (existing.isPresent()) {
                return paymentResponse(existing.get(), "Existing checkout resumed");
            }
        }

        validateRequest(req);

        if (!upiEnabled()) {
            throw new ApiException("UPI payments are currently disabled. Please contact the store.");
        }
        String upiId = upiId();
        if (upiId.isBlank()) {
            throw new ApiException("UPI payments are not configured yet. Please contact the store.");
        }
        if (!isValidUpiId(upiId)) {
            throw new ApiException("The store's UPI ID is invalid. Please contact support.");
        }

        // ── Server-side pricing from PostgreSQL ──────────────────────────
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setOrderId(OrderIdGenerator.generate());
        order.setUserId(req.getUserId());
        order.setCustomerName(req.getCustomerName().trim());
        order.setPhone(req.getPhone().trim());
        order.setEmail(isBlank(req.getEmail()) ? null : req.getEmail().trim());
        order.setAddress(req.getAddress().trim());
        order.setArea(req.getArea() == null ? "" : req.getArea().trim());
        order.setCity(req.getCity().trim());
        order.setState(req.getState() == null ? "" : req.getState().trim());
        order.setPincode(req.getPincode().trim());
        order.setPaymentMethod("UPI");
        order.setPaymentGateway(GATEWAY);
        order.setPaymentUtr("");
        order.setPaymentScreenshotUrl("");
        order.setIdempotencyKey(isBlank(req.getIdempotencyKey()) ? null : req.getIdempotencyKey().trim());
        order.setStatus(Order.OrderStatus.Pending);
        order.setPaymentStatus(Order.PaymentStatus.Pending);

        for (OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findByIdWithVariants(itemReq.getId())
                    .orElseThrow(() -> new ApiException("A product in your cart is no longer available"));

            BigDecimal price;
            String variantName = "";
            int availableStock = product.getStock() == null ? 0 : product.getStock();

            if (itemReq.getVariantId() != null) {
                ProductVariant variant = product.getVariants() == null ? null
                        : product.getVariants().stream()
                                .filter(v -> itemReq.getVariantId().equals(v.getId()))
                                .findFirst().orElse(null);
                if (variant == null) {
                    throw new ApiException("A selected variant is no longer available: " + product.getName());
                }
                price = variant.getPrice();
                variantName = variant.getVariantName() == null ? "" : variant.getVariantName();
                availableStock = variant.getStock() == null ? 0 : variant.getStock();
            } else {
                price = product.getPrice();
            }

            int qty = itemReq.getQty() == null ? 0 : itemReq.getQty();
            if (qty <= 0) {
                throw new ApiException("Invalid quantity for " + product.getName());
            }
            if (availableStock < qty) {
                throw new ApiException("Insufficient stock for " + product.getName());
            }

            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
            BigDecimal lineGst = lineTotal.multiply(nz(product.getGst()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(truncate(product.getName(), 120));
            orderItem.setVariantName(truncate(variantName, 100));
            orderItem.setProductImage(product.getImgUrl() == null ? "" : product.getImgUrl());
            orderItem.setPrice(price);
            orderItem.setGst(nz(product.getGst()));
            orderItem.setQuantity(qty);
            orderItem.setTotal(lineTotal);
            orderItem.setOrder(order);
            order.getItems().add(orderItem);

            subtotal = subtotal.add(lineTotal);
            gstAmount = gstAmount.add(lineGst);
        }

        // ── Delivery charge from zone + free-delivery threshold ──────────
        long zoneCount = deliveryZoneRepository.count();
        var zone = deliveryZoneRepository.findByPincode(order.getPincode());
        if (zoneCount > 0 && zone.isEmpty()) {
            throw new ApiException("We don't deliver to pincode " + order.getPincode() + " yet.");
        }
        BigDecimal deliveryCharge = zone.map(z -> nz(z.getCharge())).orElse(BigDecimal.ZERO);
        BigDecimal freeAbove = settingDecimal("free_delivery_above", new BigDecimal("500"));
        if (subtotal.add(gstAmount).compareTo(freeAbove) >= 0) {
            deliveryCharge = BigDecimal.ZERO;
        }

        // ── Coupon discount (server-side validation) ─────────────────────
        BigDecimal discount = BigDecimal.ZERO;
        String couponCode = req.getCouponCode() == null ? "" : req.getCouponCode().trim();
        if (!couponCode.isEmpty()) {
            Coupon coupon = couponRepository.findByCode(couponCode.toUpperCase())
                    .filter(Coupon::getIsActive)
                    .filter(c -> c.getExpiresAt() == null || c.getExpiresAt().isAfter(LocalDateTime.now()))
                    .filter(c -> c.getMaxUses() == null || (c.getUsedCount() == null ? 0 : c.getUsedCount()) < c.getMaxUses())
                    .orElseThrow(() -> new ApiException("Invalid or expired coupon code"));
            BigDecimal orderTotal = subtotal.add(gstAmount);
            if (orderTotal.compareTo(nz(coupon.getMinOrder())) < 0) {
                throw new ApiException("Minimum order ₹" + coupon.getMinOrder() + " required for this coupon");
            }
            discount = coupon.getDiscountType() == Coupon.DiscountType.percent
                    ? orderTotal.multiply(nz(coupon.getDiscountValue())).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    : nz(coupon.getDiscountValue());
        }

        BigDecimal total = subtotal.add(gstAmount).add(deliveryCharge).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Order total must be greater than zero");
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setGstAmount(gstAmount.setScale(2, RoundingMode.HALF_UP));
        order.setDeliveryCharge(deliveryCharge.setScale(2, RoundingMode.HALF_UP));
        order.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        order.setTotal(total);

        if (!couponCode.isEmpty()) {
            couponRepository.findByCode(couponCode.toUpperCase()).ifPresent(c -> {
                c.setUsedCount((c.getUsedCount() == null ? 0 : c.getUsedCount()) + 1);
                couponRepository.save(c);
            });
        }

        order = orderRepository.save(order);
        return paymentResponse(order, "Order created. Complete the UPI payment to confirm.");
    }

    /**
     * Customer clicked "I HAVE COMPLETED PAYMENT".
     * This NEVER marks the order paid — it only moves it to verification pending.
     */
    @Transactional
    public ApiResponse confirmPaymentCompleted(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPaymentStatus() == Order.PaymentStatus.Paid) {
            return paymentResponse(order, "Payment already verified");
        }
        if (order.getPaymentStatus() == Order.PaymentStatus.VerificationPending
                || order.getStatus() == Order.OrderStatus.PaymentVerificationPending) {
            return paymentResponse(order, "Payment verification already pending");
        }
        if (order.getPaymentStatus() == Order.PaymentStatus.Failed
                || order.getPaymentStatus() == Order.PaymentStatus.Cancelled
                || order.getStatus() == Order.OrderStatus.Cancelled) {
            throw new ApiException("This order can no longer be paid. Please place a new order.");
        }

        order.setPaymentStatus(Order.PaymentStatus.VerificationPending);
        order.setStatus(Order.OrderStatus.PaymentVerificationPending);
        appendNote(order, "[PAYMENT] Customer marked paid via UPI at " + LocalDateTime.now());
        order = orderRepository.save(order);
        return paymentResponse(order, "Payment submitted for verification. Our team will confirm shortly.");
    }

    /** Current payment state — used for status polling after the customer returns from a UPI app. */
    @Transactional(readOnly = true)
    public ApiResponse paymentStatus(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return ApiResponse.ok()
                .with("order_id", order.getOrderId())
                .with("status", order.getStatus())
                .with("payment_status", order.getPaymentStatus())
                .with("payment_gateway", order.getPaymentGateway())
                .with("total", order.getTotal())
                .with("payment_utr", order.getPaymentUtr())
                .with("payment_paid_at", order.getPaymentPaidAt())
                .with("created_at", order.getCreatedAt());
    }

    // ── Internals ────────────────────────────────────────────────────────

    private ApiResponse paymentResponse(Order order, String message) {
        String upiUri = buildUpiUri(order);
        return ApiResponse.ok(message)
                .with("order_id", order.getOrderId())
                .with("status", order.getStatus())
                .with("payment_status", order.getPaymentStatus())
                .with("payment_gateway", order.getPaymentGateway())
                .with("total", order.getTotal())
                .with("subtotal", order.getSubtotal())
                .with("gst_amount", order.getGstAmount())
                .with("delivery_charge", order.getDeliveryCharge())
                .with("discount", order.getDiscount())
                .with("currency", "INR")
                .with("upi_id", upiId())
                .with("payee_name", payeeName())
                .with("upi_uri", upiUri)
                .with("upi_link", upiUri)
                .with("qr_image_url", upiQrService.activeImageUrl())
                .with("qr_data_url", qrDataUrl(upiUri))
                .with("idempotency_key", order.getIdempotencyKey());
    }

    /** Standard UPI deep link — opens any UPI app with payee + exact amount prefilled. */
    private String buildUpiUri(Order order) {
        String amount = order.getTotal().setScale(2, RoundingMode.HALF_UP).toPlainString();
        return "upi://pay?pa=" + enc(upiId())
                + "&pn=" + enc(payeeName())
                + "&am=" + enc(amount)
                + "&cu=INR"
                + "&tn=" + enc(order.getOrderId());
    }

    private String qrDataUrl(String upiUri) {
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(upiUri, BarcodeFormat.QR_CODE, 320, 320, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    private boolean upiEnabled() {
        // Enabled unless admin explicitly sets upi_enabled=false
        String v = setting("upi_enabled").trim().toLowerCase();
        return !v.equals("false") && !v.equals("0") && !v.equals("no");
    }

    private boolean isValidUpiId(String id) {
        // Standard UPI handle: local-part@bank (e.g. gonaturefarms@okaxis)
        return id != null && id.matches("^[A-Za-z0-9._\\-]{2,256}@[A-Za-z][A-Za-z0-9]{1,64}$");
    }

    private String configError(boolean enabled, boolean valid) {
        if (!enabled) return "UPI payments are disabled";
        if (upiId().isBlank()) return "Merchant UPI ID is not configured";
        if (!valid) return "Merchant UPI ID is invalid";
        return "";
    }

    private String upiId() {
        return setting("upi_id").trim();
    }

    private String payeeName() {
        String name = setting("upi_payee_name").trim();
        if (name.isBlank()) name = setting("site_name").trim();
        return name.isBlank() ? "Go Nature Farms" : name;
    }

    private String setting(String key) {
        return siteSettingRepository.findByKey(key)
                .map(SiteSetting::getValue)
                .map(v -> v == null ? "" : v)
                .orElse("");
    }

    private BigDecimal settingDecimal(String key, BigDecimal fallback) {
        try {
            String v = setting(key).trim();
            return v.isBlank() ? fallback : new BigDecimal(v);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void validateRequest(OrderRequest req) {
        if (isBlank(req.getCustomerName()) || isBlank(req.getPhone()) || isBlank(req.getAddress())
                || isBlank(req.getCity()) || isBlank(req.getPincode())
                || req.getItems() == null || req.getItems().isEmpty()) {
            throw new ApiException("Missing required order fields");
        }
    }

    private void appendNote(Order order, String line) {
        String existing = order.getNotes() == null ? "" : order.getNotes();
        order.setNotes(existing.isBlank() ? line : existing + "\n" + line);
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
