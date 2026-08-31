package com.gonaturefarms.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FooterLinkRequest {
    private String name;
    private String url;
    private String category;
    private Integer sortOrder;

    // Manual getters as failsafe for Lombok processing issues
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getCategory() { return category; }
    public Integer getSortOrder() { return sortOrder; }
}
