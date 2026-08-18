package com.gonaturefarms.dto.support;

import lombok.Data;

import java.util.Map;

@Data
public class SupportTicketRequest {
    private Map<String, Object> fields;
    private Long userId;

    // Manual getters as failsafe for Lombok processing issues
    public Map<String, Object> getFields() { return fields; }
    public Long getUserId() { return userId; }
}
