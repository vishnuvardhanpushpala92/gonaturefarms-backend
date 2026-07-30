package com.gonaturefarms.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ZoneRequest {
    private String pincode;
    private String area;
    private String city;
    private String state;
    private BigDecimal charge;
}
