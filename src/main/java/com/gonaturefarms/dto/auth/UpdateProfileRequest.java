package com.gonaturefarms.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    
    @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits")
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String pincode;

    // Manual getters as failsafe for Lombok processing issues
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPincode() { return pincode; }
}
