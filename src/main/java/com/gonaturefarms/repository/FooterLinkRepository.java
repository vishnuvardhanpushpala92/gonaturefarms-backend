package com.gonaturefarms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gonaturefarms.entity.FooterLink;
import com.gonaturefarms.entity.FooterLink.LinkCategory;

@Repository
public interface FooterLinkRepository extends JpaRepository<FooterLink, Long> {
    
    /**
     * Find all footer links ordered by sort order and then by ID
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: SELECT * FROM footer_links ORDER BY sort_order ASC, id ASC
     */
    List<FooterLink> findAllByOrderBySortOrderAscIdAsc();
    
    /**
     * Find footer links by category ordered by sort order
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: SELECT * FROM footer_links WHERE category = ? ORDER BY sort_order ASC, id ASC
     */
    List<FooterLink> findByCategoryOrderBySortOrderAscIdAsc(LinkCategory category);
    
    /**
     * Delete footer links by category
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: DELETE FROM footer_links WHERE category = ?
     */
    void deleteByCategory(LinkCategory category);
}
