package com.gonaturefarms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_notification_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sound_enabled", nullable = false)
    @Builder.Default
    private Boolean soundEnabled = true;

    @Column(name = "desktop_enabled", nullable = false)
    @Builder.Default
    private Boolean desktopEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = true;
}
