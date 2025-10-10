package org.firstlab.second.controller;

import jakarta.validation.Valid;
import org.firstlab.second.dto.TicketDTO;
import org.firstlab.second.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        TicketDTO created = ticketService.createTicket(ticketDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        List<TicketDTO> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
        TicketDTO ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketDTO ticketDTO) {
        TicketDTO updated = ticketService.updateTicket(id, ticketDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketDTO> cancelTicket(@PathVariable Long id) {
        TicketDTO cancelled = ticketService.cancelTicket(id);
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/screening/{screeningId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByScreening(@PathVariable Long screeningId) {
        List<TicketDTO> tickets = ticketService.getTicketsByScreening(screeningId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByCustomer(@PathVariable Long customerId) {
        List<TicketDTO> tickets = ticketService.getTicketsByCustomer(customerId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/screening/{screeningId}/active")
    public ResponseEntity<List<TicketDTO>> getActiveTicketsByScreening(@PathVariable Long screeningId) {
        List<TicketDTO> tickets = ticketService.getActiveTicketsByScreening(screeningId);
        return ResponseEntity.ok(tickets);
    }
}
