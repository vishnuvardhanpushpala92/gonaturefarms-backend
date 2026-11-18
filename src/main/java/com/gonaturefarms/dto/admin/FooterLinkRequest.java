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
    private Boolean enabled;
    private Boolean openInNewTab;
    private String columnHeading;

    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getCategory() { return category; }
    public Integer getSortOrder() { return sortOrder; }
    public Boolean getEnabled() { return enabled; }
    public Boolean getOpenInNewTab() { return openInNewTab; }
    public String getColumnHeading() { return columnHeading; }
}
