package com.gonaturefarms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonaturefarms.entity.*;
import com.gonaturefarms.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** Builds real .xlsx exports of business data. Never includes passwords/tokens. */
@Service
public class AdminExportService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final TestimonialRepository testimonialRepository;
    private final VideoRepository videoRepository;
    private final FaqRepository faqRepository;
    private final ObjectMapper objectMapper;

    public AdminExportService(UserRepository userRepository, OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository, SupportTicketRepository supportTicketRepository,
                              ReviewRepository reviewRepository, ProductRepository productRepository,
                              TestimonialRepository testimonialRepository, VideoRepository videoRepository,
                              FaqRepository faqRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.testimonialRepository = testimonialRepository;
        this.videoRepository = videoRepository;
        this.faqRepository = faqRepository;
        this.objectMapper = objectMapper;
    }

    public static final Map<String, String> EXPORT_CATEGORIES = new LinkedHashMap<>();
    static {
        EXPORT_CATEGORIES.put("customers", "Customer Records");
        EXPORT_CATEGORIES.put("orders", "Orders");
        EXPORT_CATEGORIES.put("order_items", "Order Items");
        EXPORT_CATEGORIES.put("payments", "Payments");
        EXPORT_CATEGORIES.put("complaints", "Customer Complaints");
        EXPORT_CATEGORIES.put("reviews", "Customer Reviews");
        EXPORT_CATEGORIES.put("products", "Products");
        EXPORT_CATEGORIES.put("testimonials", "Testimonials");
        EXPORT_CATEGORIES.put("videos", "Videos");
        EXPORT_CATEGORIES.put("faqs", "FAQs");
    }

    @Transactional(readOnly = true)
    public byte[] exportXlsx(List<String> categories, LocalDate from, LocalDate to) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Summary sheet: export date + record counts
            Sheet summary = wb.createSheet("Summary");
            Row r0 = summary.createRow(0);
            r0.createCell(0).setCellValue("Export Date");
            r0.createCell(1).setCellValue(LocalDateTime.now().toString());
            Row r1 = summary.createRow(1);
            r1.createCell(0).setCellValue("Date Range");
            r1.createCell(1).setCellValue((from != null ? from : "any") + " to " + (to != null ? to : "any"));
            int sr = 3;
            for (String cat : categories) {
                Row row = summary.createRow(sr++);
                row.createCell(0).setCellValue(cat);
                row.createCell(1).setCellValue(EXPORT_CATEGORIES.getOrDefault(cat, cat));
            }
            summary.setColumnWidth(0, 30 * 256);
            summary.setColumnWidth(1, 50 * 256);

            for (String cat : categories) {
                SheetData sd = buildSheet(cat, from, to);
                if (sd == null) continue;
                Sheet sheet = wb.createSheet(sd.name);
                writeSheet(sheet, sd.headers, sd.rows);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private record SheetData(String name, List<String> headers, List<List<Object>> rows) {}

    private void writeSheet(Sheet sheet, List<String> headers, List<List<Object>> rows) {
        Row h = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) h.createCell(i).setCellValue(headers.get(i));
        for (int r = 0; r < rows.size(); r++) {
            Row row = sheet.createRow(r + 1);
            List<Object> vals = rows.get(r);
            for (int c = 0; c < vals.size(); c++) {
                Object v = vals.get(c);
                Cell cell = row.createCell(c);
                if (v == null) cell.setCellValue("");
                else if (v instanceof Number n) cell.setCellValue(n.doubleValue());
                else cell.setCellValue(String.valueOf(v));
            }
        }
        for (int i = 0; i < headers.size(); i++) sheet.autoSizeColumn(i);
    }

    private boolean inRange(LocalDateTime dt, LocalDate from, LocalDate to) {
        if (dt == null) return true;
        if (from != null && dt.toLocalDate().isBefore(from)) return false;
        return to == null || !dt.toLocalDate().isAfter(to);
    }

    private SheetData buildSheet(String cat, LocalDate from, LocalDate to) {
        return switch (cat) {
            case "customers" -> {
                List<User> users = userRepository.findAll().stream()
                        .filter(u -> u.getRole() != User.UserRole.admin)
                        .filter(u -> inRange(u.getCreatedAt(), from, to)).toList();
                yield new SheetData("Customers",
                        List.of("ID", "Name", "Phone", "Email", "Role", "Created At"),
                        users.stream().map(u -> List.<Object>of(u.getId(), u.getName(), u.getPhone(), u.getEmail(), u.getRole(), u.getCreatedAt())).toList());
            }
            case "orders" -> {
                List<Order> orders = orderRepository.findAll().stream()
                        .filter(o -> inRange(o.getCreatedAt(), from, to)).toList();
                yield new SheetData("Orders",
                        List.of("Order ID", "Tracking", "Customer", "Phone", "Status", "Payment Status", "Total", "Created At"),
                        orders.stream().map(o -> List.<Object>of(o.getId(), o.getOrderId(), o.getCustomerName(), o.getPhone(), o.getStatus(), o.getPaymentStatus(), o.getTotal(), o.getCreatedAt())).toList());
            }
            case "order_items" -> {
                List<OrderItem> items = orderItemRepository.findAll().stream()
                        .filter(i -> inRange(i.getOrder() != null ? i.getOrder().getCreatedAt() : null, from, to)).toList();
                yield new SheetData("Order Items",
                        List.of("Item ID", "Order ID", "Product", "Variant", "Qty", "Price"),
                        items.stream().map(i -> List.<Object>of(i.getId(), i.getOrder() != null ? i.getOrder().getId() : null, i.getProductName(), i.getVariantName(), i.getQuantity(), i.getPrice())).toList());
            }
            case "payments" -> {
                List<Order> orders = orderRepository.findAll().stream()
                        .filter(o -> inRange(o.getCreatedAt(), from, to)).toList();
                yield new SheetData("Payments",
                        List.of("Order ID", "Customer", "Amount", "Payment Status", "UTR", "Paid At", "Created At"),
                        orders.stream().map(o -> List.<Object>of(o.getId(), o.getCustomerName(), o.getTotal(), o.getPaymentStatus(), o.getPaymentUtr(), o.getPaymentPaidAt(), o.getCreatedAt())).toList());
            }
            case "complaints" -> {
                List<SupportTicket> tickets = supportTicketRepository.findAllByOrderByCreatedAtDesc().stream()
                        .filter(t -> inRange(t.getCreatedAt(), from, to)).toList();
                List<List<Object>> rows = new ArrayList<>();
                for (SupportTicket t : tickets) {
                    Map<String, Object> d = parseData(t.getData());
                    rows.add(List.of(t.getTicketNumber(), t.getCustomerName(), t.getCustomerMobile(), t.getCustomerEmail(),
                            t.getCategory(), d.get("subject"), d.get("description"), t.getStatus(), t.getCustomerResponse(), t.getCreatedAt()));
                }
                yield new SheetData("Complaints",
                        List.of("Ref", "Name", "Mobile", "Email", "Category", "Subject", "Description", "Status", "Response", "Submitted"), rows);
            }
            case "reviews" -> {
                List<Review> reviews = reviewRepository.findAll().stream()
                        .filter(r -> inRange(r.getCreatedAt(), from, to)).toList();
                yield new SheetData("Reviews",
                        List.of("ID", "Product", "Customer", "Rating", "Comment", "Created At"),
                        reviews.stream().map(r -> List.<Object>of(r.getId(), r.getProductId(), r.getCustomerName(), r.getRating(), r.getComment(), r.getCreatedAt())).toList());
            }
            case "products" -> {
                List<Product> products = productRepository.findAll();
                yield new SheetData("Products",
                        List.of("ID", "Name", "Category", "Price", "Stock", "Return Eligible", "Active", "Created At"),
                        products.stream().map(p -> List.<Object>of(p.getId(), p.getName(), p.getCat(), p.getPrice(), p.getStock(), p.getReturnEligible(), p.getStatus(), null)).toList());
            }
            case "testimonials" -> {
                List<Testimonial> items = testimonialRepository.findAll().stream()
                        .filter(t -> inRange(t.getCreatedAt(), from, to)).toList();
                yield new SheetData("Testimonials",
                        List.of("ID", "Name", "Message", "Rating", "Created At"),
                        items.stream().map(t -> List.<Object>of(t.getId(), t.getCustomerName(), t.getQuote(), t.getRating(), t.getCreatedAt())).toList());
            }
            case "videos" -> {
                List<Video> items = videoRepository.findAll().stream()
                        .filter(v -> inRange(v.getCreatedAt(), from, to)).toList();
                yield new SheetData("Videos",
                        List.of("ID", "Title", "URL", "Created At"),
                        items.stream().map(v -> List.<Object>of(v.getId(), v.getTitle(), v.getFilePath(), v.getCreatedAt())).toList());
            }
            case "faqs" -> {
                List<Faq> items = faqRepository.findAll().stream()
                        .filter(f -> inRange(f.getCreatedAt(), from, to)).toList();
                yield new SheetData("FAQs",
                        List.of("ID", "Question", "Answer", "Category", "Published", "Created At"),
                        items.stream().map(f -> List.<Object>of(f.getId(), f.getQuestion(), f.getAnswer(), f.getCategory() != null ? f.getCategory().getName() : null, f.getEnabled(), f.getCreatedAt())).toList());
            }
            default -> null;
        };
    }

    private Map<String, Object> parseData(String json) {
        try { return json == null ? Map.of() : objectMapper.readValue(json, Map.class); }
        catch (Exception e) { return Map.of(); }
    }
}
