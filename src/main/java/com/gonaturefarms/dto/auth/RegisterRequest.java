package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Name is required")
    private String name;

    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String pincode;

    private String puzzleAnswer;

    private String securityQuestion;

    private String securityAnswer;
}
