package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.SiteSetting;
import com.gonaturefarms.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Manages the generic key/value site_settings table used to drive admin-configurable content. */
@Service
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse publicSettings() {
        List<SiteSetting> all = siteSettingRepository.findAll();
        Map<String, String> map = new LinkedHashMap<>();
        for (SiteSetting s : all) {
            map.put(s.getKey(), s.getValue());
        }
        return ApiResponse.ok().with("settings", map);
    }

    @Transactional
    public ApiResponse update(Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            SiteSetting setting = siteSettingRepository.findByKey(entry.getKey())
                    .orElseGet(() -> SiteSetting.builder().key(entry.getKey()).build());
            setting.setValue(entry.getValue());
            setting.setUpdatedAt(java.time.LocalDateTime.now());
            siteSettingRepository.save(setting);
        }
        return ApiResponse.ok("Settings updated");
    }
}
