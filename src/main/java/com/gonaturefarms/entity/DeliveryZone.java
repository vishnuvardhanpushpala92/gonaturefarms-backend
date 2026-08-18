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
}
