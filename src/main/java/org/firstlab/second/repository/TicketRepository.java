package org.firstlab.second.repository;

import org.firstlab.second.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByScreeningId(Long screeningId);
    List<Ticket> findByCustomerId(Long customerId);
    List<Ticket> findByScreeningIdAndIsCancelled(Long screeningId, Boolean isCancelled);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.screening.id = :screeningId AND t.isCancelled = false")
    Long countActiveTicketsByScreeningId(@Param("screeningId") Long screeningId);
}

