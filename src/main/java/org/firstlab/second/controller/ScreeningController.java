package org.firstlab.second.controller;

import jakarta.validation.Valid;
import org.firstlab.second.dto.ScreeningDTO;
import org.firstlab.second.service.ScreeningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @PostMapping
    public ResponseEntity<ScreeningDTO> createScreening(@Valid @RequestBody ScreeningDTO screeningDTO) {
        ScreeningDTO created = screeningService.createScreening(screeningDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ScreeningDTO>> getAllScreenings() {
        List<ScreeningDTO> screenings = screeningService.getAllScreenings();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreeningDTO> getScreeningById(@PathVariable Long id) {
        ScreeningDTO screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScreeningDTO> updateScreening(@PathVariable Long id, @Valid @RequestBody ScreeningDTO screeningDTO) {
        ScreeningDTO updated = screeningService.updateScreening(id, screeningDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ScreeningDTO>> getUpcomingScreenings() {
        List<ScreeningDTO> screenings = screeningService.getUpcomingScreenings();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByMovie(@PathVariable Long movieId) {
        List<ScreeningDTO> screenings = screeningService.getScreeningsByMovie(movieId);
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByHall(@PathVariable Long hallId) {
        List<ScreeningDTO> screenings = screeningService.getScreeningsByHall(hallId);
        return ResponseEntity.ok(screenings);
    }
}
