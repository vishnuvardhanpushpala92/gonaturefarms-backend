package com.gonaturefarms.repository;

import com.gonaturefarms.entity.AdminDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminDeviceTokenRepository extends JpaRepository<AdminDeviceToken, Long> {
    Optional<AdminDeviceToken> findByFcmToken(String fcmToken);
    List<AdminDeviceToken> findByEnabledTrue();
    void deleteByFcmToken(String fcmToken);
}
