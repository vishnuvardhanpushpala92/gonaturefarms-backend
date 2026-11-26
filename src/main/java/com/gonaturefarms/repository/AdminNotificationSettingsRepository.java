package com.gonaturefarms.repository;

import com.gonaturefarms.entity.AdminNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminNotificationSettingsRepository extends JpaRepository<AdminNotificationSettings, Long> {
    Optional<AdminNotificationSettings> findFirstBy();
}
