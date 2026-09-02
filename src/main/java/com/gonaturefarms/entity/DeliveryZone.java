package com.gonaturefarms.entity;

import java.math.BigDecimal;

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

/** Maps to the "delivery_zones" table. */
@Entity
@Table(name = "delivery_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String pincode;

    @Builder.Default
    @Column(length = 100)
    private String area = "";

    @Builder.Default
    @Column(length = 80)
    private String city = "";

    @Builder.Default
    @Column(length = 80)
    private String state = "";

    @Builder.Default
    @Column(precision = 6, scale = 2)
    private BigDecimal charge = BigDecimal.ZERO;

    // Additional setters for manual construction
    public void setArea(String area) { this.area = area; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setCharge(BigDecimal charge) { this.charge = charge; }

    // Static builder method as failsafe for Lombok @Builder
    public static DeliveryZoneBuilder builder() {
        return new DeliveryZoneBuilder();
    }

    public static class DeliveryZoneBuilder {
        private Long id;
        private String pincode;
        private String area = "";
        private String city = "";
        private String state = "";
        private BigDecimal charge = BigDecimal.ZERO;

        public DeliveryZoneBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DeliveryZoneBuilder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public DeliveryZoneBuilder area(String area) {
            this.area = area;
            return this;
        }

        public DeliveryZoneBuilder city(String city) {
            this.city = city;
            return this;
        }

        public DeliveryZoneBuilder state(String state) {
            this.state = state;
            return this;
        }

        public DeliveryZoneBuilder charge(BigDecimal charge) {
            this.charge = charge;
            return this;
        }

        public DeliveryZone build() {
            DeliveryZone zone = new DeliveryZone();
            zone.id = this.id;
            zone.pincode = this.pincode;
            zone.area = this.area;
            zone.city = this.city;
            zone.state = this.state;
            zone.charge = this.charge;
            return zone;
        }
    }
}
