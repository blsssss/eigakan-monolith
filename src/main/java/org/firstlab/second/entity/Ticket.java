package org.firstlab.second.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Screening should be valid")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @NotNull(message = "Customer should be valid")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Seat number should be valid")
    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false)
    private LocalDateTime purchaseTime;

    @Column(nullable = false)
    private Boolean isCancelled = false;

    @PrePersist
    public void prePersist() {
        if (purchaseTime == null) {
            purchaseTime = LocalDateTime.now();
        }
        if (isCancelled == null) {
            isCancelled = false;
        }
    }
}
