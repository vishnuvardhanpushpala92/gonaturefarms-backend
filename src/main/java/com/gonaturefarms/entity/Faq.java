package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps to the "faqs" table. */
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pending = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Manual getters and setters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getPending() { return pending; }
    public void setPending(Boolean pending) { this.pending = pending; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static FaqBuilder builder() {
        return new FaqBuilder();
    }

    public static class FaqBuilder {
        private Long id;
        private String question;
        private String answer;
        private Integer sortOrder = 0;
        private Boolean pending = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public FaqBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public FaqBuilder question(String question) {
            this.question = question;
            return this;
        }

        public FaqBuilder answer(String answer) {
            this.answer = answer;
            return this;
        }

        public FaqBuilder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public FaqBuilder pending(Boolean pending) {
            this.pending = pending;
            return this;
        }

        public FaqBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Faq build() {
            Faq faq = new Faq();
            faq.id = this.id;
            faq.question = this.question;
            faq.answer = this.answer;
            faq.sortOrder = this.sortOrder;
            faq.pending = this.pending;
            faq.createdAt = this.createdAt;
            return faq;
        }
    }
}
