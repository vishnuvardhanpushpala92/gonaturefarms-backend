package com.gonaturefarms.service;

import com.gonaturefarms.entity.SupportSettings;
import com.gonaturefarms.repository.SupportSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportSettingsService {
    private static final Logger log = LoggerFactory.getLogger(SupportSettingsService.class);

    private final SupportSettingsRepository repository;

    public SupportSettingsService(SupportSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SupportSettings getSettings() {
        return repository.findAll().stream().findFirst().orElse(new SupportSettings());
    }

    @Transactional
    public SupportSettings update(SupportSettings settings) {
        try {
            // Single settings record: update the existing row if present, else insert.
            SupportSettings existing = repository.findAll().stream().findFirst().orElse(null);
            if (existing != null) {
                settings.setId(existing.getId());
                settings.setCreatedAt(existing.getCreatedAt());
            }
            SupportSettings saved = repository.save(settings);
            log.info("Support settings saved (id={})", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to save support settings: {}", e.getMessage(), e);
            throw e;
        }
    }
}
