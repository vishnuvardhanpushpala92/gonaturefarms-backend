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

    private String dateOfBirth;

    // ✅ Added fields to match User entity
    private String securityQuestion;
    private String securityAnswer;

    // Manual getters as failsafe
    public String getPhone() { return phone; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPincode() { return pincode; }
    public String getPuzzleAnswer() { return puzzleAnswer; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
}