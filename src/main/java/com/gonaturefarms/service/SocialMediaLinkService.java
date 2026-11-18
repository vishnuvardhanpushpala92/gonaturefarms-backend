package com.gonaturefarms.service;

import com.gonaturefarms.entity.SocialMediaLink;
import com.gonaturefarms.repository.SocialMediaLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocialMediaLinkService {
    private final SocialMediaLinkRepository repository;
    
    public SocialMediaLinkService(SocialMediaLinkRepository repository) {
        this.repository = repository;
    }
    
    public List<SocialMediaLink> getAll() {
        return repository.findAllByOrderBySortOrderAsc();
    }
    
    public SocialMediaLink create(SocialMediaLink link) {
        return repository.save(link);
    }
    
    public SocialMediaLink update(Long id, SocialMediaLink link) {
        link.setId(id);
        return repository.save(link);
    }
    
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
