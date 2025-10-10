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
public class HallDTO {

    private Long id;

    @NotBlank(message = "Hall name cannot be empty")
    private String name;

    @NotNull(message = "Capacity should be specified")
    @Positive(message = "Capacity should be positive")
    private Integer capacity;
}
