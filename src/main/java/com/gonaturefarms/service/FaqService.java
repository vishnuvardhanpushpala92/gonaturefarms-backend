package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.FaqRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Faq;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaqService {

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        List<Faq> allFaqs = faqRepository.findAllByOrderBySortOrderAscIdAsc();
        // Filter out pending FAQs for public view
        List<Faq> publicFaqs = allFaqs.stream()
                .filter(f -> !f.getPending())
                .collect(Collectors.toList());
        return ApiResponse.ok().with("faqs", publicFaqs);
    }

    @Transactional(readOnly = true)
    public ApiResponse listAll() {
        List<Faq> allFaqs = faqRepository.findAllByOrderBySortOrderAscIdAsc();
        // Include pending FAQs for admin view
        return ApiResponse.ok().with("faqs", allFaqs);
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
        return ApiResponse.ok("FAQ added").with("id", faq.getId());
    }

    @Transactional
    public ApiResponse delete(Long id) {
        faqRepository.deleteById(id);
        return ApiResponse.ok("FAQ deleted");
    }
}
