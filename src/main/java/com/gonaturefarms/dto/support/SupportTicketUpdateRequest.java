package com.gonaturefarms.dto.support;

import lombok.Data;

@Data
public class SupportTicketUpdateRequest {
    private String status;
    private String adminNote;
}
