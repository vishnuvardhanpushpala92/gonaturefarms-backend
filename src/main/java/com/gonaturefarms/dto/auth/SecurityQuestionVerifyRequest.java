package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SecurityQuestionVerifyRequest {
    @NotBlank(message = "Phone or email is required")
    private String identifier;

    private String captchaToken;

    // Manual getters as failsafe for Lombok processing issues
    public String getIdentifier() { return identifier; }
    public String getCaptchaToken() { return captchaToken; }
}
