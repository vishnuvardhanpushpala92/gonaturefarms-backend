package com.gonaturefarms.dto.support;

import lombok.Data;

import java.util.Map;

@Data
public class SupportTicketRequest {
    private Map<String, Object> fields;
    private Long userId;
}
