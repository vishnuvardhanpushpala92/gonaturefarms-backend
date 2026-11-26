package com.gonaturefarms.repository;

import com.gonaturefarms.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {
    List<AdminNotification> findAllByOrderByCreatedAtDesc();
    List<AdminNotification> findByIsReadFalseOrderByCreatedAtDesc();
    long countByIsReadFalse();
    boolean existsByOrderIdAndType(String orderId, String type);
}
