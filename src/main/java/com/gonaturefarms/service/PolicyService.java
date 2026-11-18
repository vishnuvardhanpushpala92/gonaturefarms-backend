package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Policy;
import com.gonaturefarms.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PolicyService {
    private final PolicyRepository repository;
    
    public PolicyService(PolicyRepository repository) {
        this.repository = repository;
    }
    
    public List<Policy> getAll() {
        return repository.findAll();
    }
    
    public Policy getByType(String type) {
        return repository.findByPolicyType(type);
    }
    
    public Policy update(Policy policy) {
        policy.setLastUpdated(LocalDateTime.now());
        return repository.save(policy);
    }
}
