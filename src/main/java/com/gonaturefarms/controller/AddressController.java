package com.gonaturefarms.controller;

import com.gonaturefarms.dto.address.AddressRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ApiResponse getUserAddresses() {
        Long userId = SecurityUtils.requireCurrentUser().id();
        return addressService.getUserAddresses(userId);
    }

    @PostMapping
    public ApiResponse createAddress(@Valid @RequestBody AddressRequest request) {
        Long userId = SecurityUtils.requireCurrentUser().id();
        return addressService.createAddress(userId, request);
    }

    @PutMapping("/{id}")
    public ApiResponse updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        Long userId = SecurityUtils.requireCurrentUser().id();
        return addressService.updateAddress(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUser().id();
        return addressService.deleteAddress(userId, id);
    }

    @PutMapping("/{id}/default")
    public ApiResponse setDefaultAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUser().id();
        return addressService.setDefaultAddress(userId, id);
    }
}
