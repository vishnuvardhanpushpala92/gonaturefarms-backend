package com.gonaturefarms.service;

import com.gonaturefarms.dto.address.AddressRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Address;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.AddressRepository;
import com.gonaturefarms.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse getUserAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return ApiResponse.ok().with("addresses", addresses);
    }

    @Transactional
    public ApiResponse createAddress(Long userId, AddressRequest request) {
        // If setting as default, unset other default addresses
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                    .filter(Address::getIsDefault)
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .userId(userId)
                .addressType(request.getAddressType())
                .name(request.getName())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        address = addressRepository.save(address);
        return ApiResponse.ok("Address created successfully").with("address", address);
    }

    @Transactional
    public ApiResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiException("Address not found"));

        // If setting as default, unset other default addresses
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                    .filter(Address::getIsDefault)
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        address.setAddressType(request.getAddressType());
        address.setName(request.getName());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setPhone(request.getPhone());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : address.getIsDefault());

        address = addressRepository.save(address);
        return ApiResponse.ok("Address updated successfully").with("address", address);
    }

    @Transactional
    public ApiResponse deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiException("Address not found"));

        addressRepository.delete(address);
        return ApiResponse.ok("Address deleted successfully");
    }

    @Transactional
    public ApiResponse setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiException("Address not found"));

        // Unset all other default addresses
        addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .filter(Address::getIsDefault)
                .forEach(addr -> {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                });

        address.setIsDefault(true);
        addressRepository.save(address);

        return ApiResponse.ok("Default address updated successfully");
    }
}
