package com.gonaturefarms.dto.admin;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ZoneRequest {
    private String pincode;
    private String area;
    private String city;
    private String state;
    private BigDecimal charge;

    // Manual getters as failsafe for Lombok processing issues
    public String getPincode() { return pincode; }
    public String getArea() { return area; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public BigDecimal getCharge() { return charge; }
}
