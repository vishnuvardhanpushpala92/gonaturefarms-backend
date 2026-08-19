package com.gonaturefarms.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier;
    private String password;

    // Manual getters as failsafe for Lombok processing issues
    public String getIdentifier() { return identifier; }
    public String getPassword() { return password; }
}
