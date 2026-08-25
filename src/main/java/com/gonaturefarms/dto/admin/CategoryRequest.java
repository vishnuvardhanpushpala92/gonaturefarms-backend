package com.gonaturefarms.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryRequest {
    private String name;

    // Manual getter as failsafe for Lombok processing issues
    public String getName() { return name; }
}
