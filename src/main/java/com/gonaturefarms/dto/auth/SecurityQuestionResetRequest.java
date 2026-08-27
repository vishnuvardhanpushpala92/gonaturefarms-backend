package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SecurityQuestionResetRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Answer is required")
    private String answer;

    @NotBlank(message = "New password is required")
    private String newPassword;
}