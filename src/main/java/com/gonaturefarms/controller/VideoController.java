package com.gonaturefarms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Video;
import com.gonaturefarms.service.VideoService;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public ApiResponse getAllEnabled() {
        return videoService.getAllEnabled();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminAll() {
        return videoService.adminAll();
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@RequestParam("title") String title,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(value = "enabled", defaultValue = "true") Boolean enabled,
                              @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder,
                              @RequestParam(value = "orientation", defaultValue = "landscape") String orientation) {
        Video video = new Video();
        video.setTitle(title);
        video.setEnabled(enabled);
        video.setSortOrder(sortOrder);
        video.setOrientation(orientation);
        return videoService.create(video, file);
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id,
                              @RequestParam("title") String title,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "enabled", defaultValue = "true") Boolean enabled,
                              @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder,
                              @RequestParam(value = "orientation", defaultValue = "landscape") String orientation) {
        Video video = new Video();
        video.setTitle(title);
        video.setEnabled(enabled);
        video.setSortOrder(sortOrder);
        video.setOrientation(orientation);
        return videoService.update(id, video, file);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return videoService.delete(id);
    }

    @PutMapping("/admin/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse toggleEnabled(@PathVariable Long id) {
        return videoService.toggleEnabled(id);
    }
}
