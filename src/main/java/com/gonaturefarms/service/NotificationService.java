package com.gonaturefarms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonaturefarms.entity.*;
import com.gonaturefarms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AdminNotificationRepository notificationRepository;
    private final AdminNotificationSettingsRepository settingsRepository;
    private final AdminDeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;

    /** All connected admin SSE clients. */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public NotificationService(AdminNotificationRepository notificationRepository,
                               AdminNotificationSettingsRepository settingsRepository,
                               AdminDeviceTokenRepository deviceTokenRepository,
                               FcmService fcmService) {
        this.notificationRepository = notificationRepository;
        this.settingsRepository = settingsRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.fcmService = fcmService;
    }

    // ---------- SSE stream ----------

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout; client reconnects
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("{\"ok\":true}"));
        } catch (IOException ignored) {}
        return emitter;
    }

    private void broadcast(AdminNotification n) {
        String payload;
        try {
            payload = MAPPER.writeValueAsString(Map.of(
                    "id", n.getId(),
                    "orderId", n.getOrderId() == null ? "" : n.getOrderId(),
                    "type", n.getType(),
                    "title", n.getTitle(),
                    "message", n.getMessage(),
                    "orderAmount", n.getOrderAmount() == null ? "" : n.getOrderAmount().toPlainString(),
                    "isRead", n.getIsRead(),
                    "createdAt", n.getCreatedAt() == null ? "" : n.getCreatedAt().toString()));
        } catch (Exception e) {
            log.warn("Failed to serialize notification: {}", e.getMessage());
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(payload));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    // ---------- Creation (after order commit only) ----------

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNewOrderPlaced(OrderService.NewOrderPlacedEvent event) {
        Order order = event.order();
        try {
            // Dedup: one notification per order+type
            if (notificationRepository.existsByOrderIdAndType(order.getOrderId(), "NEW_ORDER")) {
                return;
            }
            String customerName = order.getCustomerName() != null ? order.getCustomerName() : "Customer";
            BigDecimal amount = order.getTotal();
            AdminNotification n = AdminNotification.builder()
                    .orderId(order.getOrderId())
                    .type("NEW_ORDER")
                    .title("New Order Received")
                    .message("Order " + order.getOrderId() + " placed by " + customerName
                            + (amount != null ? " for ₹" + amount : ""))
                    .orderAmount(amount)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(n);
            broadcast(n);
            fcmService.sendToAll(n);
            log.info("Admin notification created for order {}", order.getOrderId());
        } catch (Exception e) {
            // Never let notification failure break anything post-commit
            log.error("Failed to create admin notification for order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    // ---------- Queries ----------

    @Transactional(readOnly = true)
    public List<AdminNotification> listAll() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<AdminNotification> listUnread() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    // ---------- Mutations ----------

    @Transactional
    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllRead() {
        LocalDateTime now = LocalDateTime.now();
        for (AdminNotification n : notificationRepository.findByIsReadFalseOrderByCreatedAtDesc()) {
            n.setIsRead(true);
            n.setReadAt(now);
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    // ---------- Settings ----------

    @Transactional
    public AdminNotificationSettings getSettings() {
        return settingsRepository.findFirstBy()
                .orElseGet(() -> settingsRepository.save(AdminNotificationSettings.builder().build()));
    }

    @Transactional
    public AdminNotificationSettings updateSettings(Boolean sound, Boolean desktop, Boolean push) {
        AdminNotificationSettings s = getSettings();
        if (sound != null) s.setSoundEnabled(sound);
        if (desktop != null) s.setDesktopEnabled(desktop);
        if (push != null) s.setPushEnabled(push);
        return settingsRepository.save(s);
    }

    // ---------- Device tokens ----------

    @Transactional
    public void registerDevice(Long adminId, String token, String deviceInfo) {
        if (token == null || token.isBlank()) return;
        AdminDeviceToken t = deviceTokenRepository.findByFcmToken(token)
                .orElseGet(() -> AdminDeviceToken.builder().fcmToken(token).build());
        t.setAdminId(adminId);
        t.setDeviceInfo(deviceInfo);
        t.setEnabled(true);
        t.setUpdatedAt(LocalDateTime.now());
        deviceTokenRepository.save(t);
    }

    @Transactional
    public void removeDevice(String token) {
        if (token != null) deviceTokenRepository.deleteByFcmToken(token);
    }
}
