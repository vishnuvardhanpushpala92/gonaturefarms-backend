package com.gonaturefarms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Maps to the "delivery_zones" table. */
@Entity
@Table(name = "delivery_zones")
@Getter
@Setter
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
