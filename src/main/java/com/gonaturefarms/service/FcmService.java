package com.gonaturefarms.service;

import com.gonaturefarms.entity.AdminDeviceToken;
import com.gonaturefarms.entity.AdminNotification;
import com.gonaturefarms.entity.AdminNotificationSettings;
import com.gonaturefarms.repository.AdminDeviceTokenRepository;
import com.gonaturefarms.repository.AdminNotificationSettingsRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging sender. Disabled unless firebase credentials are
 * configured via environment (service account JSON path or base64 content).
 */
@Service
public class FcmService {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    private final AdminDeviceTokenRepository deviceTokenRepository;
    private final AdminNotificationSettingsRepository settingsRepository;

    private volatile boolean initialized = false;
    private volatile boolean enabled = false;

    public FcmService(AdminDeviceTokenRepository deviceTokenRepository,
                      AdminNotificationSettingsRepository settingsRepository,
                      @Value("${firebase.service-account-json:}") String serviceAccountJson,
                      @Value("${firebase.service-account-path:}") String serviceAccountPath,
                      @Value("${firebase.enabled:false}") boolean firebaseEnabled) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.settingsRepository = settingsRepository;
        if (!firebaseEnabled) {
            log.info("FCM disabled (firebase.enabled=false)");
            return;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                java.io.File credFile;
                if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
                    credFile = new java.io.File(serviceAccountPath);
                } else if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
                    credFile = java.io.File.createTempFile("firebase-sa", ".json");
                    java.nio.file.Files.writeString(credFile.toPath(), serviceAccountJson);
                    credFile.deleteOnExit();
                } else {
                    log.warn("FCM enabled but no service account configured; push disabled");
                    return;
                }
                com.google.auth.oauth2.GoogleCredentials credentials =
                        com.google.auth.oauth2.GoogleCredentials.fromStream(new java.io.FileInputStream(credFile));
                FirebaseApp.initializeApp(com.google.firebase.FirebaseOptions.builder()
                        .setCredentials(credentials).build());
            }
            initialized = true;
            enabled = true;
            log.info("FCM initialized successfully");
        } catch (Exception e) {
            log.error("FCM initialization failed: {}", e.getMessage());
        }
    }

    public void sendToAll(AdminNotification n) {
        if (!enabled || !initialized) return;
        try {
            AdminNotificationSettings s = settingsRepository.findFirstBy().orElse(null);
            if (s != null && Boolean.FALSE.equals(s.getPushEnabled())) return;

            List<AdminDeviceToken> tokens = deviceTokenRepository.findByEnabledTrue();
            if (tokens.isEmpty()) return;

            Map<String, String> data = new HashMap<>();
            data.put("type", "NEW_ORDER");
            data.put("orderId", n.getOrderId() == null ? "" : n.getOrderId());
            data.put("url", "/admin");

            for (AdminDeviceToken t : tokens) {
                try {
                    Message msg = Message.builder()
                            .setToken(t.getFcmToken())
                            .setNotification(Notification.builder()
                                    .setTitle(n.getTitle())
                                    .setBody(n.getMessage())
                                    .build())
                            .putAllData(data)
                            .build();
                    String resp = FirebaseMessaging.getInstance().send(msg);
                    log.debug("FCM sent to token {}: {}", t.getId(), resp);
                } catch (Exception e) {
                    // Invalid/expired token — disable it
                    log.warn("FCM send failed for token {}: {}", t.getId(), e.getMessage());
                    t.setEnabled(false);
                    deviceTokenRepository.save(t);
                }
            }
        } catch (Exception e) {
            log.error("FCM broadcast failed: {}", e.getMessage());
        }
    }
}
