package com.gonaturefarms.repository;

import com.gonaturefarms.entity.ScrollBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrollBlockRepository extends JpaRepository<ScrollBlock, Long> {
    List<ScrollBlock> findAllByOrderBySortOrderAscIdAsc();

    /**
     * Find scroll blocks where pending is true or null
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: SELECT * FROM scroll_blocks WHERE pending = true OR pending IS NULL
     */
    List<ScrollBlock> findByPendingTrue();
}
