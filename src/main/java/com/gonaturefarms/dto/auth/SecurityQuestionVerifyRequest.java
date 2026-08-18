package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SecurityQuestionVerifyRequest {
    @NotBlank(message = "Phone or email is required")
    private String identifier;
}
