package com.gonaturefarms.controller;

import com.gonaturefarms.dto.admin.CredentialsUpdateRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PUT /api/admin/credentials — updates the admin login credentials. */
@RestController
@RequestMapping("/api/admin/credentials")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCredentialsController {

    private final AdminUserService adminUserService;

    public AdminCredentialsController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PutMapping
    public ApiResponse update(@RequestBody CredentialsUpdateRequest request) {
        return adminUserService.updateCredentials(request);
    }
}
