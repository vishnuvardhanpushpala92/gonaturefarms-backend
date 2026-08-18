package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Phone or email is required")
    private String identifier;

    // Manual getter as failsafe for Lombok processing issues
    public String getIdentifier() { return identifier; }
}
