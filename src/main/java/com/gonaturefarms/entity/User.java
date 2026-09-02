package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(length = 160)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 10)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.customer;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reset_code", length = 10)
    private String resetCode;

    @Column(name = "reset_code_expires_at")
    private LocalDateTime resetCodeExpiresAt;

    @Column(name = "security_question", length = 255)
    private String securityQuestion;

    @JsonIgnore
    @Column(name = "security_answer", length = 255)
    private String securityAnswer;

    @Column(name = "whatsapp_number", length = 15)
    private String whatsappNumber;

    @Column(name = "whatsapp_opt_out")
    @Builder.Default
    private Boolean whatsappOptOut = false;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    public enum UserRole {
        customer, admin
    }

    // Manual getters and setters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }

    public LocalDateTime getResetCodeExpiresAt() { return resetCodeExpiresAt; }
    public void setResetCodeExpiresAt(LocalDateTime resetCodeExpiresAt) { this.resetCodeExpiresAt = resetCodeExpiresAt; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public Boolean getWhatsappOptOut() { return whatsappOptOut; }
    public void setWhatsappOptOut(Boolean whatsappOptOut) { this.whatsappOptOut = whatsappOptOut; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    // Static builder method as failsafe for Lombok @Builder
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String name;
        private String username;
        private String phone;
        private String email;
        private String passwordHash;
        private String pincode;
        private UserRole role = UserRole.customer;
        private Boolean isVerified = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private String resetCode;
        private LocalDateTime resetCodeExpiresAt;
        private String securityQuestion;
        private String securityAnswer;
        private String whatsappNumber;
        private Boolean whatsappOptOut = false;
        private String dateOfBirth;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public UserBuilder role(UserRole role) {
            this.role = role;
            return this;
        }

        public UserBuilder isVerified(Boolean isVerified) {
            this.isVerified = isVerified;
            return this;
        }

        public UserBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserBuilder resetCode(String resetCode) {
            this.resetCode = resetCode;
            return this;
        }

        public UserBuilder resetCodeExpiresAt(LocalDateTime resetCodeExpiresAt) {
            this.resetCodeExpiresAt = resetCodeExpiresAt;
            return this;
        }

        public UserBuilder securityQuestion(String securityQuestion) {
            this.securityQuestion = securityQuestion;
            return this;
        }

        public UserBuilder securityAnswer(String securityAnswer) {
            this.securityAnswer = securityAnswer;
            return this;
        }

        public UserBuilder whatsappNumber(String whatsappNumber) {
            this.whatsappNumber = whatsappNumber;
            return this;
        }

        public UserBuilder whatsappOptOut(Boolean whatsappOptOut) {
            this.whatsappOptOut = whatsappOptOut;
            return this;
        }

        public UserBuilder dateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public User build() {
            User user = new User();
            user.id = this.id;
            user.name = this.name;
            user.username = this.username;
            user.phone = this.phone;
            user.email = this.email;
            user.passwordHash = this.passwordHash;
            user.pincode = this.pincode;
            user.role = this.role;
            user.isVerified = this.isVerified;
            user.createdAt = this.createdAt;
            user.resetCode = this.resetCode;
            user.resetCodeExpiresAt = this.resetCodeExpiresAt;
            user.securityQuestion = this.securityQuestion;
            user.securityAnswer = this.securityAnswer;
            user.whatsappNumber = this.whatsappNumber;
            user.whatsappOptOut = this.whatsappOptOut;
            user.dateOfBirth = this.dateOfBirth;
            return user;
        }
    }
}