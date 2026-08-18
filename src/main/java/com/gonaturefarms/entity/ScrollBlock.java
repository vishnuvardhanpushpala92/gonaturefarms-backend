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

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BlockStyle {
        info, promo, notice, earth
    }
}
