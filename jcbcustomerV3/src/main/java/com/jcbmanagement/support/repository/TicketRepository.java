package com.jcbmanagement.support.repository;

import com.jcbmanagement.support.model.Ticket;
import com.jcbmanagement.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomer(User customer);
    List<Ticket> findByAssignedStaff(User staff);
    List<Ticket> findByFlaggedTrue();
}
