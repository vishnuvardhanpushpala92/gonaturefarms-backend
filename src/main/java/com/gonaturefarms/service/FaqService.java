package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.FaqRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Faq;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FaqService {

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        return ApiResponse.ok().with("faqs", faqRepository.findAllByOrderBySortOrderAscIdAsc());
    }

    @Transactional
    public ApiResponse create(FaqRequest req) {
        if (req.getQuestion() == null || req.getQuestion().isBlank()
                || req.getAnswer() == null || req.getAnswer().isBlank()) {
            throw new ApiException("Question and answer required");
        }
        Faq faq = Faq.builder().question(req.getQuestion()).answer(req.getAnswer()).build();
        faq = faqRepository.save(faq);
        return ApiResponse.ok("FAQ added").with("id", faq.getId());
    }

    @Transactional
    public ApiResponse delete(Long id) {
        faqRepository.deleteById(id);
        return ApiResponse.ok("FAQ deleted");
    }
}
