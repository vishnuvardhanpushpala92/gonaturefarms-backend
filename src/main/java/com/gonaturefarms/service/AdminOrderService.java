package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Order;
import com.gonaturefarms.repository.OrderItemRepository;
import com.gonaturefarms.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Powers GET /api/admin/orders (filterable listing) and DELETE /api/admin/orders/all. */
@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminOrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list(String status, String paymentStatus) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), Order.OrderStatus.valueOf(status)));
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                predicates.add(cb.equal(root.get("paymentStatus"), Order.PaymentStatus.valueOf(paymentStatus)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Order> orders = orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok().with("orders", orders);
    }

    @Transactional
    public ApiResponse clearAll() {
        try {
            // Export all orders to Excel before clearing
            byte[] excelData = exportOrdersToExcel();
            
            // order_items has a FK to orders with cascade delete configured on the entity,
            // but we explicitly clear both tables to mirror the original two DELETE statements.
            orderRepository.deleteAll();
            
            return ApiResponse.ok("All orders cleared and exported to Excel").with("excelData", excelData);
        } catch (IOException e) {
            // If Excel export fails, still clear the orders but notify about the export failure
            orderRepository.deleteAll();
            return ApiResponse.ok("All orders cleared (Excel export failed)").with("excelData", null);
        }
    }

    private byte[] exportOrdersToExcel() throws IOException {
        List<Order> allOrders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Orders");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Order ID", "Customer Name", "Phone", "Email", "Address", "Area", "City", "State", "Pincode", "Total", "Status", "Payment Status", "Payment Method", "Created At"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (int i = 0; i < allOrders.size(); i++) {
                Order order = allOrders.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(order.getOrderId());
                row.createCell(1).setCellValue(order.getCustomerName());
                row.createCell(2).setCellValue(order.getPhone());
                row.createCell(3).setCellValue(order.getEmail() != null ? order.getEmail() : "");
                row.createCell(4).setCellValue(order.getAddress());
                row.createCell(5).setCellValue(order.getArea());
                row.createCell(6).setCellValue(order.getCity());
                row.createCell(7).setCellValue(order.getState());
                row.createCell(8).setCellValue(order.getPincode());
                row.createCell(9).setCellValue(order.getTotal().doubleValue());
                row.createCell(10).setCellValue(order.getStatus().toString());
                row.createCell(11).setCellValue(order.getPaymentStatus().toString());
                row.createCell(12).setCellValue(order.getPaymentMethod());
                row.createCell(13).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
