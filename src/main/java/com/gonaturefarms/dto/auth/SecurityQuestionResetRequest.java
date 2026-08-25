package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SecurityQuestionResetRequest {
    @NotBlank(message = "Phone or email is required")
    private String identifier;

    @NotBlank(message = "Security answer is required")
    private String securityAnswer;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    // Manual getters as failsafe for Lombok processing issues
    public String getIdentifier() { return identifier; }
    public String getSecurityAnswer() { return securityAnswer; }
    public String getNewPassword() { return newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
}
