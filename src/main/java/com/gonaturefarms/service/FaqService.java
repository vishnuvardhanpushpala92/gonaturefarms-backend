package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.FaqRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Faq;
import com.gonaturefarms.entity.FaqCategory;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.FaqRepository;
import com.gonaturefarms.repository.FaqCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaqService {

    private final FaqRepository faqRepository;
    private final FaqCategoryRepository categoryRepository;
    private final HomepageService homepageService;

    public FaqService(FaqRepository faqRepository, FaqCategoryRepository categoryRepository, HomepageService homepageService) {
        this.faqRepository = faqRepository;
        this.categoryRepository = categoryRepository;
        this.homepageService = homepageService;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        List<Faq> allFaqs = faqRepository.findAllByOrderBySortOrderAscIdAsc();
        List<Faq> publicFaqs = allFaqs.stream()
                .filter(f -> f.getPending() == null || !f.getPending())
                .collect(Collectors.toList());
        return ApiResponse.ok().with("faqs", publicFaqs);
    }

    @Transactional(readOnly = true)
    public ApiResponse listAll() {
        List<Faq> allFaqs = faqRepository.findAllByOrderBySortOrderAscIdAsc();
        return ApiResponse.ok().with("faqs", allFaqs);
    }

    @Transactional(readOnly = true)
    public ApiResponse listCategories() {
        List<FaqCategory> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return ApiResponse.ok().with("categories", categories);
    }

    @Transactional
    public ApiResponse create(FaqRequest req) {
        if (req.getQuestion() == null || req.getQuestion().isBlank()
                || req.getAnswer() == null || req.getAnswer().isBlank()) {
            throw new ApiException("Question and answer required");
        }
        Faq faq = Faq.builder()
                .question(req.getQuestion())
                .answer(req.getAnswer())
                .pending(true)
                .build();
        faq = faqRepository.save(faq);
        homepageService.evictHomepageCache();
        return ApiResponse.ok("FAQ added").with("id", faq.getId());
    }

    @Transactional
    public ApiResponse createCategory(FaqCategory category) {
        category = categoryRepository.save(category);
        return ApiResponse.ok("Category created").with("category", category);
    }

    @Transactional
    public ApiResponse delete(Long id) {
        faqRepository.deleteById(id);
        homepageService.evictHomepageCache();
        return ApiResponse.ok("FAQ deleted");
    }

    @Transactional
    public ApiResponse deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        return ApiResponse.ok("Category deleted");
    }
}
