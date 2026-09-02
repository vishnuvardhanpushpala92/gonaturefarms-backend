package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 10, message = "Phone must be exactly 10 digits")
    private String phone;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 120, message = "Name must be between 2 and 120 characters")
    private String name;

    @Size(max = 50, message = "Username must be less than 50 characters")
    private String username;

    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String pincode;

    private String puzzleAnswer;

    private String dateOfBirth;

    private String securityQuestion;

    private String securityAnswer;
}