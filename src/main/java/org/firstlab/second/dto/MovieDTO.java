package org.firstlab.second.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {

    private Long id;

    @NotBlank(message = "Movie name cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Duration should be specified")
    @Positive(message = "Duration should be positive")
    private Integer durationMinutes;

    @NotBlank(message = "Genre cannot be empty")
    private String genre;

    @NotBlank(message = "Director cannot be empty")
    private String director;

    @NotNull(message = "Year should be specified")
    private Integer year;
}
