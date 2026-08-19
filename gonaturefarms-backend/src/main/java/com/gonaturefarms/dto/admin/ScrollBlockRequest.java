package com.gonaturefarms.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScrollBlockRequest {
    private String title;
    private String content;
    private String icon;
    private String style;

    // Manual getters as failsafe for Lombok processing issues
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getIcon() { return icon; }
    public String getStyle() { return style; }
}
