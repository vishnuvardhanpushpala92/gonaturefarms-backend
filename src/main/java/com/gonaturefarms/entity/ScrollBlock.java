package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps to the "scroll_blocks" table (scrolling promo/notice banners). */
@Entity
@Table(name = "scroll_blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrollBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(length = 10)
    private String icon = "\uD83D\uDCCB";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private BlockStyle style = BlockStyle.info;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "background_color", length = 20)
    private String backgroundColor;

    @Column(name = "text_color", length = 20)
    private String textColor;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BlockStyle {
        info, promo, notice, earth
    }

    // Manual getters and setters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public BlockStyle getStyle() { return style; }
    public void setStyle(BlockStyle style) { this.style = style; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static ScrollBlockBuilder builder() {
        return new ScrollBlockBuilder();
    }

    public static class ScrollBlockBuilder {
        private Long id;
        private String title;
        private String content;
        private String icon = "\uD83D\uDCCB";
        private BlockStyle style = BlockStyle.info;
        private Integer sortOrder = 0;
        private String backgroundColor;
        private String textColor;
        private LocalDateTime createdAt = LocalDateTime.now();

        public ScrollBlockBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ScrollBlockBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ScrollBlockBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ScrollBlockBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public ScrollBlockBuilder style(BlockStyle style) {
            this.style = style;
            return this;
        }

        public ScrollBlockBuilder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public ScrollBlockBuilder backgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public ScrollBlockBuilder textColor(String textColor) {
            this.textColor = textColor;
            return this;
        }

        public ScrollBlockBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ScrollBlock build() {
            ScrollBlock scrollBlock = new ScrollBlock();
            scrollBlock.id = this.id;
            scrollBlock.title = this.title;
            scrollBlock.content = this.content;
            scrollBlock.icon = this.icon;
            scrollBlock.style = this.style;
            scrollBlock.sortOrder = this.sortOrder;
            scrollBlock.backgroundColor = this.backgroundColor;
            scrollBlock.textColor = this.textColor;
            scrollBlock.createdAt = this.createdAt;
            return scrollBlock;
        }
    }
}
