package com.gonaturefarms.repository;

import com.gonaturefarms.entity.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
    List<DeliveryZone> findAllByOrderByPincodeAsc();
    Optional<DeliveryZone> findByPincode(String pincode);

    /**
     * Find delivery zones where pending is true or null
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: SELECT * FROM delivery_zones WHERE pending = true OR pending IS NULL
     */
    List<DeliveryZone> findByPendingTrue();
}
