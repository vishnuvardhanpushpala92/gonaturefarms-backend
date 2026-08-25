package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Phone or email is required")
    private String identifier;

    @NotBlank(message = "Reset code is required")
    private String code;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    // Manual getters as failsafe for Lombok processing issues
    public String getIdentifier() { return identifier; }
    public String getCode() { return code; }
    public String getNewPassword() { return newPassword; }
}
