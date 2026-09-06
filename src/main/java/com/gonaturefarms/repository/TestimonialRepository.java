package com.gonaturefarms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gonaturefarms.entity.Testimonial;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    List<Testimonial> findByEnabledTrueOrderBySortOrderAsc();
    List<Testimonial> findByPendingTrue();
}