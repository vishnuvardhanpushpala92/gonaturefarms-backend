package com.gonaturefarms.dto.admin;

import lombok.Data;

@Data
public class CredentialsUpdateRequest {
    private String username;
    private String password;
}
