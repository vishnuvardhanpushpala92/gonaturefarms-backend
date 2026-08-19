package com.gonaturefarms.dto.admin;

import lombok.Data;

@Data
public class FaqRequest {
    private String question;
    private String answer;

    // Manual getters as failsafe for Lombok processing issues
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
}
