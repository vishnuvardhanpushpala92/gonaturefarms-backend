package com.gonaturefarms.repository;

import com.gonaturefarms.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    /**
     * All tickets ordered newest-first. The open/in_progress-first prioritization from
     * the original SQL is applied in the service layer (see ReviewRepository note).
     */
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
