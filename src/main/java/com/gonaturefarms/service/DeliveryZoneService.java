package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.ZoneRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.DeliveryZone;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.DeliveryZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
}
