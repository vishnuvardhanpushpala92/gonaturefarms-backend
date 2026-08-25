package com.gonaturefarms.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CredentialsUpdateRequest {
    private String username;
    private String password;

    // Manual getters as failsafe for Lombok processing issues
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
