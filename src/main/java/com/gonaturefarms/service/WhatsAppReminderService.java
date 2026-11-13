package com.gonaturefarms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gonaturefarms.config.WhatsAppConfig;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.whatsapp.WhatsAppReminderRequest;
import com.gonaturefarms.entity.User;
import com.gonaturefarms.entity.WhatsAppReminder;
import com.gonaturefarms.entity.Product;
import com.gonaturefarms.repository.UserRepository;
import com.gonaturefarms.repository.WhatsAppReminderRepository;
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class WhatsAppReminderService {

    private static final Logger logger = Logger.getLogger(WhatsAppReminderService.class.getName());

    // Helper method to get appropriate emoji for product
    private String getProductEmoji(String productName, String category) {
        String lowerName = productName.toLowerCase();
        String lowerCat = category != null ? category.toLowerCase() : "";

        // Product-specific emojis
        if (lowerName.contains("amla") || lowerName.contains("pickle")) return "🥭";
        if (lowerName.contains("ghee")) return "🐄";
        if (lowerName.contains("coconut") || lowerName.contains("oil")) return "🥥";
        if (lowerName.contains("groundnut")) return "🥜";
        if (lowerName.contains("milk")) return "🥛";
        if (lowerName.contains("honey")) return "🍯";
        if (lowerName.contains("fruit") || lowerName.contains("laddu")) return "🍎";
        if (lowerName.contains("curd") || lowerName.contains("yogurt")) return "🥣";
        if (lowerName.contains("butter")) return "🧈";

        // Category-based emojis
        if (lowerCat.contains("dairy")) return "🥛";
        if (lowerCat.contains("oil")) return "🫒";
        if (lowerCat.contains("pickle")) return "🥭";
        if (lowerCat.contains("sweet")) return "🍬";

        // Default emoji
        return "🌾";
    }

    private final WhatsAppReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final WhatsAppApiService whatsappApiService;
    private final WhatsAppConfig whatsappConfig;

    public WhatsAppReminderService(WhatsAppReminderRepository reminderRepository, UserRepository userRepository, OrderRepository orderRepository, ProductRepository productRepository, WhatsAppApiService whatsappApiService, WhatsAppConfig whatsappConfig) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.whatsappApiService = whatsappApiService;
        this.whatsappConfig = whatsappConfig;
    }

    @Transactional(readOnly = true)
    public ApiResponse getAllReminders(Long adminId) {
        List<WhatsAppReminder> reminders = reminderRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
        return ApiResponse.ok().with("reminders", reminders);
    }

    @Transactional
    public ApiResponse createReminder(Long adminId, WhatsAppReminderRequest request) {
        // Convert String reminderType to enum
        WhatsAppReminder.ReminderType reminderType;
        try {
            reminderType = WhatsAppReminder.ReminderType.valueOf(request.getReminderType());
        } catch (Exception e) {
            reminderType = WhatsAppReminder.ReminderType.Custom; // Default to Custom if invalid
        }

        WhatsAppReminder reminder = WhatsAppReminder.builder()
                .adminId(adminId)
                .reminderType(reminderType)
                .productId(request.getProductId())
                .message(request.getMessage())
                .scheduledAt(request.getScheduledAt())
                .status(WhatsAppReminder.ReminderStatus.Pending)
                .build();

        reminder = reminderRepository.save(reminder);
        return ApiResponse.ok("Reminder created successfully").with("reminder", reminder);
    }

    @Transactional
    public ApiResponse sendReminder(Long reminderId) {
        WhatsAppReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Reminder not found"));

        // Mock implementation - log the message instead of sending real WhatsApp
        logger.info("MOCK WhatsApp Message: " + reminder.getMessage());
        logger.info("Scheduled for: " + reminder.getScheduledAt());

        reminder.setStatus(WhatsAppReminder.ReminderStatus.Sent);
        reminder.setSentAt(LocalDateTime.now());
        reminderRepository.save(reminder);

        return ApiResponse.ok("Reminder sent successfully (mock implementation)");
    }

    @Transactional
    public ApiResponse sendToCustomers(Long adminId, WhatsAppReminderRequest request) {
        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            throw new com.gonaturefarms.exception.ApiException("Customer IDs are required");
        }

        // Convert String reminderType to enum
        WhatsAppReminder.ReminderType reminderType;
        try {
            reminderType = WhatsAppReminder.ReminderType.valueOf(request.getReminderType());
        } catch (Exception e) {
            reminderType = WhatsAppReminder.ReminderType.Custom; // Default to Custom if invalid
        }

        // If product is selected, generate dynamic message with product details
        String message = request.getMessage();
        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Product not found"));

            // Generate premium product showcase message format
            StringBuilder messageBuilder = new StringBuilder();

            // HEADER
            messageBuilder.append("══════════════════════════════════\n");
            messageBuilder.append("🌿 Go Nature Farms\n");
            messageBuilder.append("══════════════════════════════════\n");
            messageBuilder.append("Farm-fresh organic products delivered to your door.\n\n");

            // PRODUCT DETAILS (no image URL in text)
            messageBuilder.append("✨ Introducing: ").append(product.getName()).append("\n\n");

            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                messageBuilder.append(product.getDescription()).append("\n\n");
            }

            messageBuilder.append("💰 Price: ₹").append(product.getPrice()).append("\n");
            if (product.getMrp() != null && product.getMrp().compareTo(java.math.BigDecimal.ZERO) > 0) {
                messageBuilder.append("🏷️ MRP: ₹").append(product.getMrp()).append("\n");
            }

            messageBuilder.append("\n🌱 Fresh • Natural • Quality Assured\n\n");

            // FOOTER
            messageBuilder.append("══════════════════════════════════\n");
            messageBuilder.append("🌿 Go Nature Farms\n");
            messageBuilder.append("══════════════════════════════════\n\n");
            messageBuilder.append("🛒 Shop Now:\n");
            messageBuilder.append("https://gonaturefarms-frontend.vercel.app/");

            message = messageBuilder.toString();

            // Log the message to verify emoji encoding
            logger.info("Generated WhatsApp message for product: " + product.getName());
            logger.info("Message: " + message);
            logger.info("Message contains emojis: " + message.matches(".*[🥜✨💰🏷️🌿🌱🛒].*"));
        } else {
            // Log custom message to verify emoji encoding
            logger.info("Custom WhatsApp message: " + message);
            logger.info("Custom message contains emojis: " + message.matches(".*[🥜✨💰🏷️🌿🌱🛒].*"));
        }

        // Check if WhatsApp API is configured
        boolean useWhatsAppApi = whatsappConfig.isConfigured();
        logger.info("WhatsApp API configured: " + useWhatsAppApi);

        Map<String, Object> results = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        // Upload logo image once if using WhatsApp API (to avoid repeated uploads)
        String mediaId = null;
        if (useWhatsAppApi) {
            try {
                String logoUrl = whatsappConfig.getLogoUrl();
                if (logoUrl == null || logoUrl.isEmpty()) {
                    logger.warning("WhatsApp logo URL not configured, using fallback mode");
                    useWhatsAppApi = false;
                } else {
                    logger.info("Uploading Go Nature Farms logo to WhatsApp Media API...");
                    mediaId = whatsappApiService.uploadMedia(logoUrl);
                    logger.info("Logo uploaded successfully, media ID: " + mediaId);
                }
            } catch (IOException e) {
                logger.severe("Failed to upload logo to WhatsApp: " + e.getMessage());
                errors.add("Logo upload failed: " + e.getMessage());
                // Continue with fallback mode
                useWhatsAppApi = false;
            }
        }

        for (Long customerId : request.getCustomerIds()) {
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Customer not found"));

            // Check if customer has opted out
            if (Boolean.TRUE.equals(customer.getWhatsappOptOut())) {
                logger.info("Customer " + customer.getName() + " has opted out of WhatsApp reminders");
                errors.add(customer.getName() + " opted out of WhatsApp");
                failureCount++;
                continue;
            }

            String phoneNumber = whatsappApiService.normalizePhoneNumber(customer.getPhone());

            try {
                if (useWhatsAppApi && mediaId != null) {
                    // Use WhatsApp Business Cloud API for image message with media ID (logo)
                    String caption = "🌿 Go Nature Farms\n\n" +
                            "✨ " + product.getName() + "\n\n" +
                            "💰 Price: ₹" + product.getPrice() + "\n" +
                            (product.getMrp() != null && product.getMrp().compareTo(java.math.BigDecimal.ZERO) > 0 ?
                                    "🏷️ MRP: ₹" + product.getMrp() + "\n" : "") +
                            "\n🌱 Fresh • Natural • Quality Assured\n\n" +
                            "🛒 Shop Now: https://gonaturefarms-frontend.vercel.app/";

                    JsonNode response = whatsappApiService.sendImageMessage(phoneNumber, mediaId, caption);
                    logger.info("WhatsApp API message sent to " + customer.getName() + ": " + response);
                    successCount++;
                } else {
                    // Fallback to wa.me click-to-chat links
                    String encodedMessage = java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
                    String whatsappLink = "https://wa.me/" + phoneNumber + "?text=" + encodedMessage;
                    logger.info("WhatsApp link generated for " + customer.getName() + " (" + phoneNumber + ")");
                    results.put("link_" + customerId, whatsappLink);
                    successCount++;
                }
            } catch (IOException e) {
                logger.severe("Failed to send WhatsApp message to " + customer.getName() + ": " + e.getMessage());
                errors.add(customer.getName() + ": " + e.getMessage());
                failureCount++;

                // Fallback to wa.me link on API failure
                String encodedMessage = java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
                String whatsappLink = "https://wa.me/" + phoneNumber + "?text=" + encodedMessage;
                results.put("fallback_link_" + customerId, whatsappLink);
            }
        }

        // Create a reminder record
        WhatsAppReminder reminder = WhatsAppReminder.builder()
                .adminId(adminId)
                .reminderType(reminderType)
                .productId(request.getProductId())
                .message(message)
                .scheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now())
                .status(WhatsAppReminder.ReminderStatus.Sent)
                .sentAt(LocalDateTime.now())
                .build();

        reminderRepository.save(reminder);

        // Build response
        ApiResponse response;
        if (useWhatsAppApi) {
            response = ApiResponse.ok("WhatsApp messages sent via API. Success: " + successCount + ", Failed: " + failureCount)
                    .with("successCount", successCount)
                    .with("failureCount", failureCount)
                    .with("errors", errors);
        } else {
            response = ApiResponse.ok("WhatsApp links generated for " + successCount + " customers (API not configured, using fallback)")
                    .with("whatsappLinks", results.values())
                    .with("successCount", successCount)
                    .with("failureCount", failureCount)
                    .with("errors", errors);
        }

        return response;
    }

    @Transactional
    public ApiResponse deleteReminder(Long reminderId) {
        reminderRepository.findById(reminderId)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Reminder not found"));
        reminderRepository.deleteById(reminderId);
        return ApiResponse.ok("Reminder deleted successfully");
    }

    @Transactional
    public ApiResponse generateProductReminder(Long adminId, Long productId, String productName) {
        // Fetch product details to generate dynamic message
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Product not found"));

        // Generate premium product showcase message format
        StringBuilder messageBuilder = new StringBuilder();
        
        // HEADER
        messageBuilder.append("══════════════════════════════════\n");
        messageBuilder.append("🌿 Go Nature Farms\n");
        messageBuilder.append("══════════════════════════════════\n");
        messageBuilder.append("Farm-fresh organic products delivered to your door.\n\n");

        // PRODUCT DETAILS (no image URL in text)
        messageBuilder.append("✨ Introducing: ").append(product.getName()).append("\n\n");
        
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            messageBuilder.append(product.getDescription()).append("\n\n");
        }
        
        messageBuilder.append("💰 Price: ₹").append(product.getPrice()).append("\n");
        if (product.getMrp() != null && product.getMrp().compareTo(java.math.BigDecimal.ZERO) > 0) {
            messageBuilder.append("🏷️ MRP: ₹").append(product.getMrp()).append("\n");
        }
        
        messageBuilder.append("\n🌱 Fresh • Natural • Quality Assured\n\n");
        
        // FOOTER
        messageBuilder.append("══════════════════════════════════\n");
        messageBuilder.append("🌿 Go Nature Farms\n");
        messageBuilder.append("══════════════════════════════════\n\n");
        messageBuilder.append("🛒 Shop Now:\n");
        messageBuilder.append("https://gonaturefarms-frontend.vercel.app/");

        String message = messageBuilder.toString();

        // Generate reminders for all customers who haven't opted out
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.customer)
                .filter(u -> !Boolean.TRUE.equals(u.getWhatsappOptOut()))
                .toList();

        for (User customer : customers) {
            WhatsAppReminder reminder = WhatsAppReminder.builder()
                    .adminId(adminId)
                    .reminderType(WhatsAppReminder.ReminderType.Product)
                    .productId(productId)
                    .message(message)
                    .scheduledAt(LocalDateTime.now())
                    .status(WhatsAppReminder.ReminderStatus.Pending)
                    .build();
            reminderRepository.save(reminder);
        }

        return ApiResponse.ok("Generated " + customers.size() + " product reminders for customers");
    }

    @Transactional(readOnly = true)
    public ApiResponse getCustomersByProduct(Long productId) {
        try {
            // Use optimized database query to find customer IDs
            Set<Long> customerIds = orderRepository.findCustomerIdsByProductId(productId);

            if (customerIds.isEmpty()) {
                logger.info("No customers found for product ID: " + productId);
                return ApiResponse.ok("No customers have purchased this product")
                        .with("customers", List.of());
            }

            // Fetch customers by IDs
            List<User> customers = userRepository.findAllById(customerIds).stream()
                    .filter(u -> u.getRole() == User.UserRole.customer)
                    .filter(u -> !Boolean.TRUE.equals(u.getWhatsappOptOut()))
                    .collect(Collectors.toList());

            logger.info("Found " + customers.size() + " customers for product ID: " + productId);
            return ApiResponse.ok().with("customers", customers);
        } catch (Exception e) {
            logger.severe("Error fetching customers by product ID " + productId + ": " + e.getMessage());
            return ApiResponse.ok("Error fetching customers. Please try again.")
                    .with("customers", List.of());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse getCustomersByCategory(String category) {
        // Find all products in this category
        List<Product> productsInCategory = productRepository.findAll().stream()
                .filter(p -> p.getCat() != null && p.getCat().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        
        if (productsInCategory.isEmpty()) {
            return ApiResponse.ok("No products found in this category")
                    .with("customers", List.of());
        }
        
        // Find customers who ordered any product from this category
        List<com.gonaturefarms.entity.Order> orders = orderRepository.findAll();
        Set<Long> customerIds = new HashSet<>();
        
        for (com.gonaturefarms.entity.Order order : orders) {
            if (order.getItems() != null) {
                for (com.gonaturefarms.entity.OrderItem item : order.getItems()) {
                    if (item.getProductId() != null) {
                        // Check if this product is in the category
                        for (Product product : productsInCategory) {
                            if (product.getId().equals(item.getProductId())) {
                                customerIds.add(order.getUserId());
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        List<User> customers = userRepository.findAllById(customerIds).stream()
                .filter(u -> u.getRole() == User.UserRole.customer)
                .filter(u -> !Boolean.TRUE.equals(u.getWhatsappOptOut()))
                .collect(Collectors.toList());
        
        return ApiResponse.ok().with("customers", customers);
    }

    @Transactional(readOnly = true)
    public ApiResponse checkDuplicateReminders(Long adminId, WhatsAppReminderRequest request) {
        // Check if similar reminder was sent recently (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        List<WhatsAppReminder> recentReminders = reminderRepository.findByAdminIdAndCreatedAtAfter(adminId, sevenDaysAgo);
        
        // Count customers who would receive duplicate reminders
        int duplicateCount = 0;
        if (request.getProductId() != null && request.getReminderType() != null && 
            request.getReminderType().equalsIgnoreCase("Product")) {
            for (Long customerId : request.getCustomerIds()) {
                for (WhatsAppReminder reminder : recentReminders) {
                    if (reminder.getReminderType() == WhatsAppReminder.ReminderType.Product &&
                        reminder.getProductId() != null && reminder.getProductId().equals(request.getProductId())) {
                        duplicateCount++;
                        break;
                    }
                }
            }
        }
        
        if (duplicateCount > 0) {
            return ApiResponse.ok("Found " + duplicateCount + " customers who already received this product reminder recently")
                    .with("duplicateCount", duplicateCount)
                    .with("hasDuplicates", true);
        }
        
        return ApiResponse.ok("No duplicate reminders found")
                .with("duplicateCount", 0)
                .with("hasDuplicates", false);
    }
}