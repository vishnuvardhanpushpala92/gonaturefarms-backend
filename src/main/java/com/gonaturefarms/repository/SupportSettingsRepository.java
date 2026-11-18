package com.gonaturefarms.repository;

import com.gonaturefarms.entity.SupportSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportSettingsRepository extends JpaRepository<SupportSettings, Long> {
}
