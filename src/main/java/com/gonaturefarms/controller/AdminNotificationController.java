package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.AdminNotification;
import com.gonaturefarms.entity.AdminNotificationSettings;
import com.gonaturefarms.service.NotificationService;
import com.gonaturefarms.security.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** SSE stream — admin receives real-time notifications. */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SecurityUtils.requireCurrentUser(); // ensure authenticated admin context
        return notificationService.subscribe();
    }

    @GetMapping
    public ApiResponse list(@RequestParam(defaultValue = "all") String filter) {
        List<AdminNotification> list = "unread".equalsIgnoreCase(filter)
                ? notificationService.listUnread()
                : notificationService.listAll();
        return ApiResponse.ok().with("notifications", list).with("count", list.size());
    }

    @GetMapping("/unread-count")
    public ApiResponse unreadCount() {
        return ApiResponse.ok().with("count", notificationService.unreadCount());
    }

    @PutMapping("/{id}/read")
    public ApiResponse markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok("Marked as read");
    }

    @PutMapping("/read-all")
    public ApiResponse markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.ok("All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ApiResponse.ok("Notification deleted");
    }

    // ---------- Settings ----------

    @GetMapping("/settings")
    public ApiResponse getSettings() {
        AdminNotificationSettings s = notificationService.getSettings();
        return ApiResponse.ok()
                .with("sound_enabled", s.getSoundEnabled())
                .with("desktop_enabled", s.getDesktopEnabled())
                .with("push_enabled", s.getPushEnabled());
    }

    @PutMapping("/settings")
    public ApiResponse updateSettings(@RequestBody Map<String, Object> body) {
        Boolean sound = body.get("sound_enabled") instanceof Boolean b ? b : null;
        Boolean desktop = body.get("desktop_enabled") instanceof Boolean b ? b : null;
        Boolean push = body.get("push_enabled") instanceof Boolean b ? b : null;
        AdminNotificationSettings s = notificationService.updateSettings(sound, desktop, push);
        return ApiResponse.ok("Settings updated")
                .with("sound_enabled", s.getSoundEnabled())
                .with("desktop_enabled", s.getDesktopEnabled())
                .with("push_enabled", s.getPushEnabled());
    }

    // ---------- FCM device tokens ----------

    @PostMapping("/devices")
    public ApiResponse registerDevice(@RequestBody Map<String, String> body) {
        Long adminId = SecurityUtils.requireCurrentUser().id();
        notificationService.registerDevice(adminId, body.get("token"), body.get("device_info"));
        return ApiResponse.ok("Device registered");
    }

    @DeleteMapping("/devices")
    public ApiResponse removeDevice(@RequestBody Map<String, String> body) {
        notificationService.removeDevice(body.get("token"));
        return ApiResponse.ok("Device removed");
    }
}
