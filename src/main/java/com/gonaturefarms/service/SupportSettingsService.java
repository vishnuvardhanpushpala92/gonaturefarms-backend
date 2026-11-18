package com.gonaturefarms.service;

import com.gonaturefarms.entity.SupportSettings;
import com.gonaturefarms.repository.SupportSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SupportSettingsService {
    private final SupportSettingsRepository repository;
    
    public SupportSettingsService(SupportSettingsRepository repository) {
        this.repository = repository;
    }
    
    public SupportSettings getSettings() {
        return repository.findById(1L).orElse(new SupportSettings());
    }
    
    public SupportSettings update(SupportSettings settings) {
        if (settings.getId() == null) settings.setId(1L);
        return repository.save(settings);
    }
}
