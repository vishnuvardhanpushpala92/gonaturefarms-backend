package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private FaqCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Builder.Default
    @Column
    private Boolean pending = false;

    @Builder.Default
    @Column
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FaqCategory getCategory() { return category; }
    public void setCategory(FaqCategory category) { this.category = category; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getPending() { return pending; }
    public void setPending(Boolean pending) { this.pending = pending; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static FaqBuilder builder() {
        return new FaqBuilder();
    }

    public static class FaqBuilder {
        private Long id;
        private FaqCategory category;
        private String question;
        private String answer;
        private Integer sortOrder = 0;
        private Boolean pending = false;
        private Boolean enabled = true;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        public FaqBuilder id(Long id) { this.id = id; return this; }
        public FaqBuilder category(FaqCategory category) { this.category = category; return this; }
        public FaqBuilder question(String question) { this.question = question; return this; }
        public FaqBuilder answer(String answer) { this.answer = answer; return this; }
        public FaqBuilder sortOrder(Integer sortOrder) { this.sortOrder = sortOrder; return this; }
        public FaqBuilder pending(Boolean pending) { this.pending = pending; return this; }
        public FaqBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public FaqBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FaqBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Faq build() {
            Faq faq = new Faq();
            faq.id = this.id;
            faq.category = this.category;
            faq.question = this.question;
            faq.answer = this.answer;
            faq.sortOrder = this.sortOrder;
            faq.pending = this.pending;
            faq.enabled = this.enabled;
            faq.createdAt = this.createdAt;
            faq.updatedAt = this.updatedAt;
            return faq;
        }
    }
}

