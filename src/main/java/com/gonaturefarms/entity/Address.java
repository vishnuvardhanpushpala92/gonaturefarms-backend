package com.gonaturefarms.entity;

import java.time.LocalDateTime;

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
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AddressType {
        Home, Office
    }

    // Manual getters and setters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public AddressType getAddressType() { return addressType; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    public static class AddressBuilder {
        private Long id;
        private Long userId;
        private AddressType addressType;
        private String name;
        private String addressLine;
        private String city;
        private String state;
        private String pincode;
        private String phone;
        private Boolean isDefault = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        public AddressBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AddressBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AddressBuilder addressType(AddressType addressType) {
            this.addressType = addressType;
            return this;
        }

        public AddressBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AddressBuilder addressLine(String addressLine) {
            this.addressLine = addressLine;
            return this;
        }

        public AddressBuilder city(String city) {
            this.city = city;
            return this;
        }

        public AddressBuilder state(String state) {
            this.state = state;
            return this;
        }

        public AddressBuilder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public AddressBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public AddressBuilder isDefault(Boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public AddressBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AddressBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Address build() {
            Address address = new Address();
            address.id = this.id;
            address.userId = this.userId;
            address.addressType = this.addressType;
            address.name = this.name;
            address.addressLine = this.addressLine;
            address.city = this.city;
            address.state = this.state;
            address.pincode = this.pincode;
            address.phone = this.phone;
            address.isDefault = this.isDefault;
            address.createdAt = this.createdAt;
            address.updatedAt = this.updatedAt;
            return address;
        }
    }
}