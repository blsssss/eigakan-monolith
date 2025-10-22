package org.firstlab.second.service;

import org.firstlab.second.dto.BulkPurchaseRequest;
import org.firstlab.second.dto.TicketDTO;
import org.firstlab.second.entity.Customer;
import org.firstlab.second.entity.Screening;
import org.firstlab.second.entity.Ticket;
import org.firstlab.second.repository.CustomerRepository;
import org.firstlab.second.repository.ScreeningRepository;
import org.firstlab.second.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;
    private final CustomerRepository customerRepository;
    private final ScreeningService screeningService;
    private final CustomerService customerService;

    public TicketService(TicketRepository ticketRepository,
                        ScreeningRepository screeningRepository,
                        CustomerRepository customerRepository,
                        ScreeningService screeningService,
                        CustomerService customerService) {
        this.ticketRepository = ticketRepository;
        this.screeningRepository = screeningRepository;
        this.customerRepository = customerRepository;
        this.screeningService = screeningService;
        this.customerService = customerService;
    }

    public TicketDTO createTicket(TicketDTO ticketDTO) {
        Screening screening = screeningRepository.findById(ticketDTO.getScreeningId())
                .orElseThrow(() -> new RuntimeException("Screening with ID " + ticketDTO.getScreeningId() + " not found"));

        Customer customer = customerRepository.findById(ticketDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer with ID " + ticketDTO.getCustomerId() + " not found"));

        Long activeTickets = ticketRepository.countActiveTicketsByScreeningId(screening.getId());
        if (activeTickets >= screening.getHall().getCapacity()) {
            throw new RuntimeException("No seats available for this screening");
        }

        List<Ticket> existingTickets = ticketRepository.findByScreeningIdAndIsCancelled(screening.getId(), false);
        boolean seatTaken = existingTickets.stream()
                .anyMatch(t -> t.getSeatNumber().equals(ticketDTO.getSeatNumber()));

        if (seatTaken) {
            throw new RuntimeException("Seat number " + ticketDTO.getSeatNumber() + " is already taken");
        }

        if (ticketDTO.getSeatNumber() > screening.getHall().getCapacity()) {
            throw new RuntimeException("Seat number " + ticketDTO.getSeatNumber() + " is out of bounds");
        }

        Ticket ticket = new Ticket();
        ticket.setScreening(screening);
        ticket.setCustomer(customer);
        ticket.setSeatNumber(ticketDTO.getSeatNumber());
        ticket.setPurchaseTime(LocalDateTime.now());
        ticket.setIsCancelled(false);

        screening.setAvailableSeats(screening.getAvailableSeats() - 1);
        screeningRepository.save(screening);

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket, true);
    }

    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(t -> convertToDTO(t, true))
                .collect(Collectors.toList());
    }

    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket with ID " + id + " not found"));
        return convertToDTO(ticket, true);
    }

    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket with ID " + id + " not found"));

        if (ticketDTO.getSeatNumber() != null && !ticketDTO.getSeatNumber().equals(ticket.getSeatNumber())) {
            // Проверка: новое место не занято
            List<Ticket> existingTickets = ticketRepository.findByScreeningIdAndIsCancelled(
                    ticket.getScreening().getId(), false);
            boolean seatTaken = existingTickets.stream()
                    .filter(t -> !t.getId().equals(id))
                    .anyMatch(t -> t.getSeatNumber().equals(ticketDTO.getSeatNumber()));

            if (seatTaken) {
                throw new RuntimeException("Ticket with seat number " + ticketDTO.getSeatNumber() + " already exists");
            }

            ticket.setSeatNumber(ticketDTO.getSeatNumber());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return convertToDTO(updatedTicket, true);
    }

    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new RuntimeException("Ticket with ID " + id + " not found");
        }

        Ticket ticket = ticketRepository.findById(id).get();
        Screening screening = ticket.getScreening();

        screening.setAvailableSeats(screening.getAvailableSeats() + 1);
        screeningRepository.save(screening);

        ticketRepository.deleteById(id);
    }

    public TicketDTO cancelTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket with ID " + id + " not found"));

        if (LocalDateTime.now().isAfter(ticket.getScreening().getStartTime())) {
            throw new RuntimeException("Impossible to cancel ticket after the screening has started");
        }

        if (ticket.getIsCancelled()) {
            throw new RuntimeException("Ticket is already cancelled");
        }

        ticket.setIsCancelled(true);

        // Увеличиваем количество доступных мест
        Screening screening = ticket.getScreening();
        screening.setAvailableSeats(screening.getAvailableSeats() + 1);
        screeningRepository.save(screening);

        Ticket cancelledTicket = ticketRepository.save(ticket);
        return convertToDTO(cancelledTicket, true);
    }

    public List<TicketDTO> getTicketsByScreening(Long screeningId) {
        return ticketRepository.findByScreeningId(screeningId).stream()
                .map(t -> convertToDTO(t, true))
                .collect(Collectors.toList());
    }

    public List<TicketDTO> getTicketsByCustomer(Long customerId) {
        return ticketRepository.findByCustomerId(customerId).stream()
                .map(t -> convertToDTO(t, true))
                .collect(Collectors.toList());
    }

    public List<TicketDTO> getActiveTicketsByScreening(Long screeningId) {
        return ticketRepository.findByScreeningIdAndIsCancelled(screeningId, false).stream()
                .map(t -> convertToDTO(t, true))
                .collect(Collectors.toList());
    }

    /**
     * Business Operation: Bulk Purchase - Buy multiple tickets at once
     * This is a transactional operation that ensures all tickets are purchased together or none at all
     */
    public List<TicketDTO> bulkPurchaseTickets(BulkPurchaseRequest request) {
        // Validate screening exists
        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new RuntimeException("Screening with ID " + request.getScreeningId() + " not found"));

        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer with ID " + request.getCustomerId() + " not found"));

        // Check if there are enough seats available
        Long activeTickets = ticketRepository.countActiveTicketsByScreeningId(screening.getId());
        int requestedSeats = request.getSeatNumbers().size();
        long availableSeats = screening.getHall().getCapacity() - activeTickets;

        if (requestedSeats > availableSeats) {
            throw new RuntimeException("Not enough seats available. Requested: " + requestedSeats +
                    ", Available: " + availableSeats);
        }

        // Get all currently taken seats for this screening
        List<Ticket> existingTickets = ticketRepository.findByScreeningIdAndIsCancelled(screening.getId(), false);
        List<Integer> takenSeats = existingTickets.stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toList());

        // Check if any requested seat is already taken
        List<Integer> conflictingSeats = request.getSeatNumbers().stream()
                .filter(takenSeats::contains)
                .collect(Collectors.toList());

        if (!conflictingSeats.isEmpty()) {
            throw new RuntimeException("The following seats are already taken: " + conflictingSeats);
        }

        // Check if any seat number exceeds hall capacity
        Integer maxRequestedSeat = request.getSeatNumbers().stream()
                .max(Integer::compareTo)
                .orElse(0);

        if (maxRequestedSeat > screening.getHall().getCapacity()) {
            throw new RuntimeException("Seat number " + maxRequestedSeat +
                    " exceeds hall capacity of " + screening.getHall().getCapacity());
        }

        // Check for duplicate seat numbers in request
        long uniqueSeats = request.getSeatNumbers().stream().distinct().count();
        if (uniqueSeats != request.getSeatNumbers().size()) {
            throw new RuntimeException("Duplicate seat numbers in request are not allowed");
        }

        // All checks passed - create all tickets in one transaction
        List<Ticket> tickets = new ArrayList<>();
        LocalDateTime purchaseTime = LocalDateTime.now();

        for (Integer seatNumber : request.getSeatNumbers()) {
            Ticket ticket = new Ticket();
            ticket.setScreening(screening);
            ticket.setCustomer(customer);
            ticket.setSeatNumber(seatNumber);
            ticket.setPurchaseTime(purchaseTime);
            ticket.setIsCancelled(false);
            tickets.add(ticket);
        }

        // Save all tickets
        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        // Update available seats
        screening.setAvailableSeats(screening.getAvailableSeats() - requestedSeats);
        screeningRepository.save(screening);

        // Convert to DTOs and return
        return savedTickets.stream()
                .map(t -> convertToDTO(t, true))
                .collect(Collectors.toList());
    }

    private TicketDTO convertToDTO(Ticket ticket, boolean includeDetails) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setScreeningId(ticket.getScreening().getId());
        dto.setCustomerId(ticket.getCustomer().getId());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setPurchaseTime(ticket.getPurchaseTime());
        dto.setIsCancelled(ticket.getIsCancelled());

        if (includeDetails) {
            dto.setScreening(screeningService.getScreeningById(ticket.getScreening().getId()));
            dto.setCustomer(customerService.getCustomerById(ticket.getCustomer().getId()));
        }

        return dto;
    }
}

