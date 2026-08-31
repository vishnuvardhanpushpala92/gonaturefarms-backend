package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findAllByOrderBySortOrderAscIdAsc();
    @Query("SELECT f FROM Faq f WHERE f.pending = true OR f.pending IS NULL")
    List<Faq> findByPendingTrue();
}
