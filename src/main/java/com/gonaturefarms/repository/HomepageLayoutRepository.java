package com.gonaturefarms.repository;

import com.gonaturefarms.entity.HomepageLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomepageLayoutRepository extends JpaRepository<HomepageLayout, Long> {

    /**
     * Find the currently published layout
     */
    Optional<HomepageLayout> findByIsPublishedTrueOrderByUpdatedAtDesc();

    /**
     * Find the latest draft layout
     */
    Optional<HomepageLayout> findByIsDraftTrueOrderByUpdatedAtDesc();

    /**
     * Find the most recent layout (published or draft)
     */
    Optional<HomepageLayout> findFirstByOrderByUpdatedAtDesc();
}
