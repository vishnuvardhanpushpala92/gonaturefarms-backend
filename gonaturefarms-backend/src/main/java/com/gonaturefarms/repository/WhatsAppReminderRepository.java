package com.gonaturefarms.repository;

import com.gonaturefarms.entity.WhatsAppReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhatsAppReminderRepository extends JpaRepository<WhatsAppReminder, Long> {
    List<WhatsAppReminder> findByAdminIdOrderByCreatedAtDesc(Long adminId);
    List<WhatsAppReminder> findByStatusOrderByCreatedAtDesc(WhatsAppReminder.ReminderStatus status);
}
