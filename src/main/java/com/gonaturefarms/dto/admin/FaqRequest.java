package com.gonaturefarms.dto.admin;

import lombok.Data;

@Data
public class FaqRequest {
    private String question;
    private String answer;
}
