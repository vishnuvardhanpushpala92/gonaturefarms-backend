package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.coupon.CouponRequest;
import com.gonaturefarms.dto.coupon.CouponValidateRequest;
import com.gonaturefarms.entity.Coupon;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/** Business logic for discount coupons. Mirrors routes/coupons.js. */
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse validate(CouponValidateRequest req) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new ApiException("Coupon code required");
        }
        Coupon coupon = couponRepository.findByCode(req.getCode().toUpperCase().trim())
                .filter(Coupon::getIsActive)
                .filter(c -> c.getExpiresAt() == null || c.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(c -> c.getUsedCount() < c.getMaxUses())
                .orElseThrow(() -> new ApiException("Invalid or expired coupon code"));

        BigDecimal orderTotal = req.getOrderTotal() == null ? BigDecimal.ZERO : req.getOrderTotal();
        if (orderTotal.compareTo(coupon.getMinOrder()) < 0) {
            throw new ApiException("Minimum order \u20B9" + coupon.getMinOrder() + " required for this coupon");
        }

        BigDecimal discount = coupon.getDiscountType() == Coupon.DiscountType.percent
                ? orderTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : coupon.getDiscountValue();

        return ApiResponse.ok("Coupon applied! You save \u20B9" + discount)
                .with("coupon", java.util.Map.of(
                        "code", coupon.getCode(),
                        "type", coupon.getDiscountType(),
                        "value", coupon.getDiscountValue()))
                .with("discount", discount);
    }

    @Transactional(readOnly = true)
    public ApiResponse listAll() {
        List<Coupon> coupons = couponRepository.findAllByOrderByCreatedAtDesc();
        return ApiResponse.ok().with("coupons", coupons);
    }

    @Transactional
    public ApiResponse create(CouponRequest req) {
        if (req.getCode() == null || req.getCode().isBlank() || req.getDiscountValue() == null) {
            throw new ApiException("Code and value required");
        }
        String code = req.getCode().toUpperCase().trim();
        if (couponRepository.existsByCode(code)) {
            throw new ApiException("Coupon code already exists");
        }
        Coupon coupon = Coupon.builder()
                .code(code)
                .discountType(parseType(req.getDiscountType()))
                .discountValue(req.getDiscountValue())
                .minOrder(req.getMinOrder() == null ? BigDecimal.ZERO : req.getMinOrder())
                .maxUses(req.getMaxUses() == null ? 9999 : req.getMaxUses())
                .expiresAt(req.getExpiresAt())
                .build();
        couponRepository.save(coupon);
        return ApiResponse.ok("Coupon created");
    }

    @Transactional
    public ApiResponse toggle(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Coupon not found"));
        coupon.setIsActive(!coupon.getIsActive());
        couponRepository.save(coupon);
        return ApiResponse.ok("Coupon toggled");
    }

    @Transactional
    public ApiResponse delete(Long id) {
        couponRepository.deleteById(id);
        return ApiResponse.ok("Coupon deleted");
    }

    private Coupon.DiscountType parseType(String type) {
        if (type == null || type.isBlank()) return Coupon.DiscountType.flat;
        try {
            return Coupon.DiscountType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return Coupon.DiscountType.flat;
        }
    }
}
