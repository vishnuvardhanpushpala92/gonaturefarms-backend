package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.ScrollBlockRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.ScrollBlock;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.ScrollBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScrollBlockService {

    private final ScrollBlockRepository scrollBlockRepository;

    public ScrollBlockService(ScrollBlockRepository scrollBlockRepository) {
        this.scrollBlockRepository = scrollBlockRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        return ApiResponse.ok().with("blocks", scrollBlockRepository.findAllByOrderBySortOrderAscIdAsc());
    }

    @Transactional
    public ApiResponse create(ScrollBlockRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank()
                || req.getContent() == null || req.getContent().isBlank()) {
            throw new ApiException("Title and content required");
        }
        ScrollBlock block = ScrollBlock.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .icon(req.getIcon() == null || req.getIcon().isBlank() ? "\uD83D\uDCCB" : req.getIcon())
                .style(parseStyle(req.getStyle()))
                .build();
        block = scrollBlockRepository.save(block);
        return ApiResponse.ok("Block added").with("id", block.getId());
    }

    @Transactional
    public ApiResponse delete(Long id) {
        scrollBlockRepository.deleteById(id);
        return ApiResponse.ok("Block deleted");
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
