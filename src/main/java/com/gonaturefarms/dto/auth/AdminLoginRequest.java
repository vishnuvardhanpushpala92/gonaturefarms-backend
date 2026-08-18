package com.gonaturefarms.dto.auth;

import lombok.Data;

@Data
public class AdminLoginRequest {
    private String username;
    private String password;

    // Manual getters as failsafe for Lombok processing issues
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
