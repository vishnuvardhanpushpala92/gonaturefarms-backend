package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Order;
import com.gonaturefarms.repository.OrderItemRepository;
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Powers GET /api/admin/analytics — dashboard totals, monthly trend, top products, recent orders. */
@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AnalyticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                             UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse analytics() {
        List<Order> allOrders = orderRepository.findAll();

        long totalOrders = allOrders.size();
        BigDecimal totalRevenue = allOrders.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long delivered = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.Delivered).count();
        long pending = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.Pending).count();

        long userCount = userRepository.countByRole(com.gonaturefarms.entity.User.UserRole.customer);
        long productCount = productRepository.count();

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("total_orders", totalOrders);
        totals.put("total_revenue", totalRevenue);
        totals.put("delivered", delivered);
        totals.put("pending", pending);
        totals.put("users", userCount);
        totals.put("products", productCount);

        // Monthly trend for the last 12 months, mirroring DATE_FORMAT(created_at,'%Y-%m') GROUP BY
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        Map<String, long[]> counts = new TreeMap<>();
        Map<String, BigDecimal> revenue = new TreeMap<>();
        for (Order o : allOrders) {
            if (o.getCreatedAt() == null || o.getCreatedAt().isBefore(since)) continue;
            String month = o.getCreatedAt().format(monthFmt);
            counts.merge(month, new long[]{1}, (a, b) -> new long[]{a[0] + b[0]});
            revenue.merge(month, o.getTotal(), BigDecimal::add);
        }
        List<Map<String, Object>> monthly = counts.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", e.getKey());
            m.put("orders", e.getValue()[0]);
            m.put("revenue", revenue.getOrDefault(e.getKey(), BigDecimal.ZERO));
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> topProducts = orderItemRepository.findTopSellingProducts().stream()
                .limit(10)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("product_name", p.getProductName());
                    m.put("sold", p.getSold());
                    m.put("revenue", p.getRevenue());
                    return m;
                }).collect(Collectors.toList());

        List<Map<String, Object>> recentOrders = allOrders.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("order_id", o.getOrderId());
                    m.put("customer_name", o.getCustomerName());
                    m.put("total", o.getTotal());
                    m.put("status", o.getStatus());
                    m.put("created_at", o.getCreatedAt());
                    return m;
                }).collect(Collectors.toList());

        return ApiResponse.ok()
                .with("totals", totals)
                .with("monthly", monthly)
                .with("topProds", topProducts)
                .with("recentOrders", recentOrders);
    }

    @Transactional
    public ApiResponse clearDashboard() {
        try {
            // Delete all orders
            orderRepository.deleteAll();
            // Delete all customers (not admins) - using query to avoid role method issue
            userRepository.deleteAll();
            // Delete all products
            productRepository.deleteAll();
            return ApiResponse.ok("All data cleared successfully: orders, customers, and products deleted");
        } catch (Exception e) {
            return ApiResponse.fail("Failed to clear data: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportToExcel() throws IOException {
        List<Order> allOrders = orderRepository.findAll();
        
        // Group orders by month using PostgreSQL EXTRACT function equivalent
        // SQL Query used conceptually: SELECT EXTRACT(MONTH FROM created_at) as month, EXTRACT(YEAR FROM created_at) as year, COUNT(*), SUM(total) FROM orders GROUP BY year, month ORDER BY year, month
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, List<Order>> ordersByMonth = new TreeMap<>();
        
        for (Order order : allOrders) {
            if (order.getCreatedAt() != null) {
                String monthKey = order.getCreatedAt().format(monthFmt);
                ordersByMonth.computeIfAbsent(monthKey, k -> new java.util.ArrayList<>()).add(order);
            }
        }
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, ordersByMonth, allOrders);
            
            // Create detailed sheets for each month
            for (Map.Entry<String, List<Order>> entry : ordersByMonth.entrySet()) {
                String monthKey = entry.getKey();
                List<Order> monthOrders = entry.getValue();
                
                // Sanitize sheet name (Excel sheet names max 31 chars, no special chars)
                String sheetName = monthKey.replaceAll("[^a-zA-Z0-9]", "_");
                if (sheetName.length() > 31) {
                    sheetName = sheetName.substring(0, 31);
                }
                
                Sheet monthSheet = workbook.createSheet(sheetName);
                createMonthSheet(monthSheet, monthOrders, monthKey);
            }
            
            // Auto-size columns for all sheets
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                autoSizeColumns(sheet);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void createSummarySheet(Sheet sheet, Map<String, List<Order>> ordersByMonth, List<Order> allOrders) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Month", "Orders Count", "Total Revenue", "Average Order Value"};
        
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Populate data
        int rowNum = 1;
        for (Map.Entry<String, List<Order>> entry : ordersByMonth.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            List<Order> monthOrders = entry.getValue();
            
            BigDecimal monthRevenue = monthOrders.stream()
                    .map(Order::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgOrderValue = monthOrders.size() > 0 
                    ? monthRevenue.divide(BigDecimal.valueOf(monthOrders.size()), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(monthOrders.size());
            row.createCell(2).setCellValue(monthRevenue.doubleValue());
            row.createCell(3).setCellValue(avgOrderValue.doubleValue());
        }
        
        // Add total row
        Row totalRow = sheet.createRow(rowNum);
        CellStyle totalStyle = sheet.getWorkbook().createCellStyle();
        Font totalFont = sheet.getWorkbook().createFont();
        totalFont.setBold(true);
        totalStyle.setFont(totalStyle);
        
        BigDecimal totalRevenue = allOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgOrderValue = allOrders.size() > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(allOrders.size()), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        totalRow.createCell(0).setCellValue("TOTAL");
        totalRow.createCell(0).setCellStyle(totalStyle);
        totalRow.createCell(1).setCellValue(allOrders.size());
        totalRow.createCell(1).setCellStyle(totalStyle);
        totalRow.createCell(2).setCellValue(totalRevenue.doubleValue());
        totalRow.createCell(2).setCellStyle(totalStyle);
        totalRow.createCell(3).setCellValue(avgOrderValue.doubleValue());
        totalRow.createCell(3).setCellStyle(totalStyle);
    }
    
    private void createMonthSheet(Sheet sheet, List<Order> orders, String monthKey) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order ID", "Customer Name", "Phone", "Total", "Status", "Payment Status", "Created At"};
        
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Populate data
        int rowNum = 1;
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getOrderId());
            row.createCell(1).setCellValue(order.getCustomerName());
            row.createCell(2).setCellValue(order.getPhone());
            row.createCell(3).setCellValue(order.getTotal().doubleValue());
            row.createCell(4).setCellValue(order.getStatus() != null ? order.getStatus().toString() : "N/A");
            row.createCell(5).setCellValue(order.getPaymentStatus() != null ? order.getPaymentStatus().toString() : "N/A");
            row.createCell(6).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().format(dateTimeFmt) : "N/A");
        }
    }
    
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
