package org.firstlab.second.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "halls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hall name cannot be empty")
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull(message = "Capacity should be specified")
    @Positive(message = "Capacity should be positive")
    @Column(nullable = false)
    private Integer capacity;
}
