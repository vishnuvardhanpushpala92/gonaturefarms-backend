package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByEnabledTrueOrderBySortOrderAsc();
    @Query("SELECT v FROM Video v WHERE v.pending = true OR v.pending IS NULL")
    List<Video> findByPendingTrue();
}
