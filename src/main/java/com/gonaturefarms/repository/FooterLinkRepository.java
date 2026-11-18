package com.gonaturefarms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gonaturefarms.entity.FooterLink;
import com.gonaturefarms.entity.FooterLink.LinkCategory;

@Repository
public interface FooterLinkRepository extends JpaRepository<FooterLink, Long> {
    
    List<FooterLink> findAllByOrderBySortOrderAscIdAsc();
    List<FooterLink> findTop10ByOrderBySortOrderAscIdAsc();
    List<FooterLink> findTop8ByOrderBySortOrderAscIdAsc();
    List<FooterLink> findByCategoryOrderBySortOrderAscIdAsc(LinkCategory category);
    void deleteByCategory(LinkCategory category);
    List<FooterLink> findByPendingTrue();
    
    @Query("SELECT fl FROM FooterLink fl WHERE fl.enabled = true ORDER BY fl.sortOrder ASC")
    List<FooterLink> findEnabledByOrderBySortOrderAsc();
}
