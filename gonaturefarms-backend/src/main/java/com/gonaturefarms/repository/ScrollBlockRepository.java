package com.gonaturefarms.repository;

import com.gonaturefarms.entity.ScrollBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrollBlockRepository extends JpaRepository<ScrollBlock, Long> {
    List<ScrollBlock> findAllByOrderBySortOrderAscIdAsc();
}
