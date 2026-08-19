package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps to the "site_settings" table (simple key/value store used by the admin panel). */
@Entity
@Table(name = "site_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true, length = 80)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Manual getters and setters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static SiteSettingBuilder builder() {
        return new SiteSettingBuilder();
    }

    public static class SiteSettingBuilder {
        private Long id;
        private String key;
        private String value;
        private LocalDateTime updatedAt = LocalDateTime.now();

        public SiteSettingBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SiteSettingBuilder key(String key) {
            this.key = key;
            return this;
        }

        public SiteSettingBuilder value(String value) {
            this.value = value;
            return this;
        }

        public SiteSettingBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SiteSetting build() {
            SiteSetting setting = new SiteSetting();
            setting.id = this.id;
            setting.key = this.key;
            setting.value = this.value;
            setting.updatedAt = this.updatedAt;
            return setting;
        }
    }
}
