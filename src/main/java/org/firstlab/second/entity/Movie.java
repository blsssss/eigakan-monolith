package org.firstlab.second.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description cannot be empty")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Duration should be specified")
    @Positive(message = "Duration should be positive")
    @Column(nullable = false)
    private Integer durationMinutes;

    @NotBlank(message = "Genre cannot be empty")
    private String genre;

    @NotBlank(message = "Director cannot be empty")
    private String director;

    @Column(nullable = false, name = "movie_year")
    private Integer year;
}
