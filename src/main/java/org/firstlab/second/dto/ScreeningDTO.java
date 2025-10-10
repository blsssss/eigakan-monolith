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
public class ScreeningDTO {

    private Long id;

    @NotNull(message = "Movie ID should be specified")
    private Long movieId;

    @NotNull(message = "Hall ID should be specified")
    private Long hallId;

    @NotNull(message = "Time should be specified")
    private LocalDateTime startTime;

    @NotNull(message = "Price should be specified")
    @Positive(message = "Price should be positive")
    private Double price;

    private Integer availableSeats;

    // Для ответа с полными данными
    private MovieDTO movie;
    private HallDTO hall;
}
