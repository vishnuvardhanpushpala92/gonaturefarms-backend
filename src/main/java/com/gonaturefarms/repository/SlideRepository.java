package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Slide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlideRepository extends JpaRepository<Slide, Long> {
    List<Slide> findAllByOrderBySortOrderAscIdAsc();

    /**
     * Find slides where pending is true or null
     * JPA Repository Method: Uses Spring Data JPA's automatic query generation
     * SQL Equivalent: SELECT * FROM slides WHERE pending = true OR pending IS NULL
     */
    List<Slide> findByPendingTrue();
}
