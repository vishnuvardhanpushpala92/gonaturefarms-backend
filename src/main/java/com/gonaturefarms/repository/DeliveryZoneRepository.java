package com.gonaturefarms.repository;

import com.gonaturefarms.entity.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
    List<DeliveryZone> findAllByOrderByPincodeAsc();
    Optional<DeliveryZone> findByPincode(String pincode);
}
