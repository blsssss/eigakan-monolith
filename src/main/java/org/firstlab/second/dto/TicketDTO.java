package org.firstlab.second.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private Long id;

    @NotNull(message = "Screening ID should be specified")
    private Long screeningId;

    @NotNull(message = "Customer ID should be specified")
    private Long customerId;

    @NotNull(message = "Seat number should be specified")
    @Positive(message = "Seat number should be positive")
    private Integer seatNumber;

    private LocalDateTime purchaseTime;

    private Boolean isCancelled;

    // Для ответа с полными данными
    private ScreeningDTO screening;
    private CustomerDTO customer;
}
