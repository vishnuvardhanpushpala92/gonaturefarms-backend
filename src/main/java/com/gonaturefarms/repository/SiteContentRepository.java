package com.gonaturefarms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gonaturefarms.entity.SiteContent;

@Repository
public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {
    
    /**
     * Find site content by slug (e.g., 'about-us')
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: SELECT * FROM site_content WHERE slug = ? LIMIT 1
     */
    Optional<SiteContent> findBySlug(String slug);
    
    /**
     * Check if a slug exists
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: SELECT COUNT(*) FROM site_content WHERE slug = ?
     */
    boolean existsBySlug(String slug);
}
