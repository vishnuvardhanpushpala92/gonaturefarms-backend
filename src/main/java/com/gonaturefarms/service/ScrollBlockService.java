package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.ScrollBlockRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.ScrollBlock;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.ScrollBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrollBlockService {

    private final ScrollBlockRepository scrollBlockRepository;

    public ScrollBlockService(ScrollBlockRepository scrollBlockRepository) {
        this.scrollBlockRepository = scrollBlockRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        List<ScrollBlock> allBlocks = scrollBlockRepository.findAllByOrderBySortOrderAscIdAsc();
        // Filter out pending blocks for public view
        List<ScrollBlock> activeBlocks = allBlocks.stream()
                .filter(block -> block.getPending() == null || !block.getPending())
                .collect(Collectors.toList());
        return ApiResponse.ok().with("blocks", activeBlocks);
    }

    @Transactional(readOnly = true)
    public ApiResponse listAll() {
        List<ScrollBlock> allBlocks = scrollBlockRepository.findAllByOrderBySortOrderAscIdAsc();
        // Include pending blocks for admin view
        return ApiResponse.ok().with("blocks", allBlocks);
    }

    @Transactional
    public ApiResponse create(ScrollBlockRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new ApiException("Title is required");
        }
        
        // Use custom icon if provided, otherwise use regular icon or default
        String iconValue = req.getIcon();
        if ("custom".equals(iconValue) && req.getCustomIcon() != null && !req.getCustomIcon().isBlank()) {
            iconValue = req.getCustomIcon();
        } else if (iconValue == null || iconValue.isBlank()) {
            iconValue = "\uD83D\uDCCB"; // Default icon
        }
        
        ScrollBlock block = ScrollBlock.builder()
                .title(req.getTitle())
                .content(req.getContent() != null ? req.getContent() : req.getTitle()) // Default to title if content is empty
                .icon(iconValue)
                .style(parseStyle(req.getStyle()))
                .backgroundColor(req.getBackgroundColor())
                .textColor(req.getTextColor())
                .pending(true)
                .build();
        block = scrollBlockRepository.save(block);
        return ApiResponse.ok("Block added").with("id", block.getId());
    }

    @Transactional
    public ApiResponse delete(Long id) {
        if (!scrollBlockRepository.existsById(id)) {
            throw new ApiException("Scroll block not found");
        }
        scrollBlockRepository.deleteById(id);
        return ApiResponse.ok("Block deleted");
    }

    @Transactional
    public ApiResponse update(Long id, ScrollBlockRequest req) {
        ScrollBlock block = scrollBlockRepository.findById(id)
                .orElseThrow(() -> new ApiException("Scroll block not found"));
        
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            block.setTitle(req.getTitle());
        }
        
        if (req.getContent() != null) {
            block.setContent(req.getContent());
        }
        
        // Update icon if provided
        if (req.getIcon() != null) {
            String iconValue = req.getIcon();
            if ("custom".equals(iconValue) && req.getCustomIcon() != null && !req.getCustomIcon().isBlank()) {
                iconValue = req.getCustomIcon();
            } else if (iconValue.isBlank()) {
                iconValue = "\uD83D\uDCCB";
            }
            block.setIcon(iconValue);
        }
        
        if (req.getStyle() != null) {
            block.setStyle(parseStyle(req.getStyle()));
        }
        
        if (req.getBackgroundColor() != null) {
            block.setBackgroundColor(req.getBackgroundColor());
        }
        
        if (req.getTextColor() != null) {
            block.setTextColor(req.getTextColor());
        }

        block.setPending(true);
        block = scrollBlockRepository.save(block);
        return ApiResponse.ok("Block updated successfully");
    }

    private ScrollBlock.BlockStyle parseStyle(String style) {
        if (style == null || style.isBlank()) return ScrollBlock.BlockStyle.info;
        try {
            return ScrollBlock.BlockStyle.valueOf(style);
        } catch (IllegalArgumentException e) {
            return ScrollBlock.BlockStyle.info;
        }
    }
}
