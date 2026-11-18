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

@Entity
@Table(name = "support_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "support_phone", length = 20)
    private String supportPhone;

    @Column(name = "whatsapp_number", length = 20)
    private String whatsappNumber;

    @Column(name = "whatsapp_country_code", length = 10)
    @Builder.Default
    private String whatsappCountryCode = "91";

    @Column(name = "support_email", length = 100)
    private String supportEmail;

    @Column(name = "business_hours", length = 200)
    private String businessHours;

    @Column(name = "working_days", length = 100)
    private String workingDays;

    @Column(name = "store_address", columnDefinition = "TEXT")
    private String storeAddress;

    @Column(name = "whatsapp_welcome_message", columnDefinition = "TEXT")
    private String whatsappWelcomeMessage;

    @Column(name = "response_info", columnDefinition = "TEXT")
    private String responseInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSupportPhone() { return supportPhone; }
    public void setSupportPhone(String supportPhone) { this.supportPhone = supportPhone; }
    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
    public String getWhatsappCountryCode() { return whatsappCountryCode; }
    public void setWhatsappCountryCode(String whatsappCountryCode) { this.whatsappCountryCode = whatsappCountryCode; }
    public String getSupportEmail() { return supportEmail; }
    public void setSupportEmail(String supportEmail) { this.supportEmail = supportEmail; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public String getWhatsappWelcomeMessage() { return whatsappWelcomeMessage; }
    public void setWhatsappWelcomeMessage(String whatsappWelcomeMessage) { this.whatsappWelcomeMessage = whatsappWelcomeMessage; }
    public String getResponseInfo() { return responseInfo; }
    public void setResponseInfo(String responseInfo) { this.responseInfo = responseInfo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
