package com.gonaturefarms.repository;

import com.gonaturefarms.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    List<SupportTicket> findByCategory(String category);
    List<SupportTicket> findByOrderId(Long orderId);
    List<SupportTicket> findByStatus(SupportTicket.TicketStatus status);
}
