package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/data")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDataDeletionController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private CouponRepository couponRepository;
    
    @Autowired
    private SupportRepository supportRepository;

    @Transactional
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponse> deleteAllUsers() {
        try {
            // Delete all customer addresses first
            addressRepository.deleteAll();
            
            // Delete all users except admin by filtering
            List<com.gonaturefarms.entity.User> users = userRepository.findAll();
            for (com.gonaturefarms.entity.User user : users) {
                if (user.getRole() != com.gonaturefarms.entity.User.UserRole.admin) {
                    userRepository.delete(user);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.ok("All users deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Failed to delete users: " + e.getMessage()));
        }
    }

    @Transactional
    @DeleteMapping("/orders")
    public ResponseEntity<ApiResponse> deleteAllOrders() {
        try {
            orderRepository.deleteAll();
            return ResponseEntity.ok(ApiResponse.ok("All orders deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Failed to delete orders: " + e.getMessage()));
        }
    }

    @Transactional
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse> deleteAllData() {
        try {
            // Delete in order of dependencies (products are NOT deleted)
            addressRepository.deleteAll();
            orderRepository.deleteAll();
            couponRepository.deleteAll();
            supportRepository.deleteAll();
            
            // Delete all users except admin
            List<com.gonaturefarms.entity.User> users = userRepository.findAll();
            for (com.gonaturefarms.entity.User user : users) {
                if (user.getRole() != com.gonaturefarms.entity.User.UserRole.admin) {
                    userRepository.delete(user);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.ok("All data deleted successfully (products preserved)"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Failed to delete all data: " + e.getMessage()));
        }
    }
}