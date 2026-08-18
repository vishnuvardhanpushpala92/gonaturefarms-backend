package com.gonaturefarms.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressRequest {
    @NotNull(message = "Address type is required")
    private com.gonaturefarms.entity.Address.AddressType addressType;

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @NotBlank(message = "Address line is required")
    @Size(max = 255, message = "Address line must not exceed 255 characters")
    private String addressLine;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    private String pincode;

    @NotBlank(message = "Phone is required")
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;

    private Boolean isDefault;

    // Manual getters as failsafe for Lombok processing issues
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public String getPhone() { return phone; }
    public Boolean getIsDefault() { return isDefault; }
    public String getName() { return name; }
    public String getAddressLine() { return addressLine; }
    public String getCity() { return city; }
}
