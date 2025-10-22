package org.firstlab.second.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BulkPurchaseRequest {

    @NotNull(message = "Screening ID cannot be null")
    private Long screeningId;

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotEmpty(message = "Seat numbers list cannot be empty")
    private List<Integer> seatNumbers;

    public Long getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Long screeningId) {
        this.screeningId = screeningId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<Integer> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<Integer> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
}

