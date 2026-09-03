package com.gonaturefarms.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.admin.ZoneRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.DeliveryZone;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.DeliveryZoneRepository;

@Service
public class DeliveryZoneService {

    private final DeliveryZoneRepository deliveryZoneRepository;

    public DeliveryZoneService(DeliveryZoneRepository deliveryZoneRepository) {
        this.deliveryZoneRepository = deliveryZoneRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        return ApiResponse.ok().with("zones", deliveryZoneRepository.findAllByOrderByPincodeAsc());
    }

    @Transactional
    public ApiResponse upsert(ZoneRequest req) {
        if (req.getPincode() == null || req.getPincode().isBlank()) {
            throw new ApiException("Pincode required");
        }
        String pincode = req.getPincode().trim();
        DeliveryZone zone = deliveryZoneRepository.findByPincode(pincode)
                .orElseGet(() -> DeliveryZone.builder().pincode(pincode).build());
        zone.setArea(req.getArea() == null ? "" : req.getArea());
        zone.setCity(req.getCity() == null ? "" : req.getCity());
        zone.setState(req.getState() == null ? "" : req.getState());
        zone.setCharge(req.getCharge() == null ? BigDecimal.ZERO : req.getCharge());
        deliveryZoneRepository.save(zone);
        return ApiResponse.ok("Zone saved");
    }

    @Transactional
    public ApiResponse delete(Long id) {
        deliveryZoneRepository.deleteById(id);
        return ApiResponse.ok("Zone deleted");
    }

    @Transactional(readOnly = true)
    public ApiResponse validatePincode(String pincode) {
        if (pincode == null || pincode.isBlank()) {
            return ApiResponse.fail("Pincode is required");
        }
        String trimmedPincode = pincode.trim();
        boolean exists = deliveryZoneRepository.findByPincode(trimmedPincode).isPresent();
        if (!exists) {
            return ApiResponse.fail("Invalid pincode. Delivery not available in your area.");
        }
        return ApiResponse.ok("Pincode is valid for delivery");
    }

    @Transactional(readOnly = true)
    public ApiResponse getDeliveryCharge(String pincode) {
        if (pincode == null || pincode.isBlank()) {
            return ApiResponse.fail("Pincode is required");
        }
        String trimmedPincode = pincode.trim();
        return deliveryZoneRepository.findByPincode(trimmedPincode)
                .map(zone -> ApiResponse.ok().with("charge", zone.getCharge()).with("area", zone.getArea()))
                .orElse(ApiResponse.fail("Delivery not available in your area"));
    }
}
