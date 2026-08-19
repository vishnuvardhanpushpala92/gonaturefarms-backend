package com.gonaturefarms.controller;

import com.gonaturefarms.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/admin/export/{type} — CSV export of orders, users, or the monthly report. */
@RestController
@RequestMapping("/api/admin/export")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExportController {

    private final ExportService exportService;

    public AdminExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/{type}")
    public ResponseEntity<?> export(@PathVariable String type) {
        if (!java.util.List.of("orders", "users", "monthly").contains(type)) {
            return ResponseEntity.badRequest().body(com.gonaturefarms.dto.common.ApiResponse.fail("Unknown export type"));
        }
        ExportService.CsvFile file = exportService.export(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file.content());
    }
}
