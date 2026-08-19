package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Slide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlideRepository extends JpaRepository<Slide, Long> {
    List<Slide> findAllByOrderBySortOrderAscIdAsc();
}
