package org.firstlab.second.service;

import org.firstlab.second.dto.ScreeningDTO;
import org.firstlab.second.entity.Hall;
import org.firstlab.second.entity.Movie;
import org.firstlab.second.entity.Screening;
import org.firstlab.second.repository.HallRepository;
import org.firstlab.second.repository.MovieRepository;
import org.firstlab.second.repository.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final MovieService movieService;
    private final HallService hallService;

    public ScreeningService(ScreeningRepository screeningRepository,
                           MovieRepository movieRepository,
                           HallRepository hallRepository,
                           MovieService movieService,
                           HallService hallService) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.hallRepository = hallRepository;
        this.movieService = movieService;
        this.hallService = hallService;
    }

    public ScreeningDTO createScreening(ScreeningDTO screeningDTO) {
        Movie movie = movieRepository.findById(screeningDTO.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie with ID " + screeningDTO.getMovieId() + " not found"));

        Hall hall = hallRepository.findById(screeningDTO.getHallId())
                .orElseThrow(() -> new RuntimeException("Hall with ID " + screeningDTO.getHallId() + " not found"));

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(screeningDTO.getStartTime());
        screening.setPrice(screeningDTO.getPrice());
        screening.setAvailableSeats(hall.getCapacity());

        Screening savedScreening = screeningRepository.save(screening);
        return convertToDTO(savedScreening, true);
    }

    public List<ScreeningDTO> getAllScreenings() {
        return screeningRepository.findAll().stream()
                .map(s -> convertToDTO(s, true))
                .collect(Collectors.toList());
    }

    public ScreeningDTO getScreeningById(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Screening with ID " + id + " not found"));
        return convertToDTO(screening, true);
    }

    public ScreeningDTO updateScreening(Long id, ScreeningDTO screeningDTO) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Screening with ID " + id + " not found"));

        if (screeningDTO.getMovieId() != null) {
            Movie movie = movieRepository.findById(screeningDTO.getMovieId())
                    .orElseThrow(() -> new RuntimeException("Movie with ID " + screeningDTO.getMovieId() + " not found"));
            screening.setMovie(movie);
        }

        if (screeningDTO.getHallId() != null) {
            Hall hall = hallRepository.findById(screeningDTO.getHallId())
                    .orElseThrow(() -> new RuntimeException("Hall with ID " + screeningDTO.getHallId() + " not found"));
            screening.setHall(hall);
        }

        if (screeningDTO.getStartTime() != null) {
            screening.setStartTime(screeningDTO.getStartTime());
        }

        if (screeningDTO.getPrice() != null) {
            screening.setPrice(screeningDTO.getPrice());
        }

        Screening updatedScreening = screeningRepository.save(screening);
        return convertToDTO(updatedScreening, true);
    }

    public void deleteScreening(Long id) {
        if (!screeningRepository.existsById(id)) {
            throw new RuntimeException("Screening with ID " + id + " not found");
        }
        screeningRepository.deleteById(id);
    }

    public List<ScreeningDTO> getUpcomingScreenings() {
        return screeningRepository.findUpcomingScreenings(LocalDateTime.now()).stream()
                .map(s -> convertToDTO(s, true))
                .collect(Collectors.toList());
    }

    public List<ScreeningDTO> getScreeningsByMovie(Long movieId) {
        return screeningRepository.findByMovieId(movieId).stream()
                .map(s -> convertToDTO(s, true))
                .collect(Collectors.toList());
    }

    public List<ScreeningDTO> getScreeningsByHall(Long hallId) {
        return screeningRepository.findByHallId(hallId).stream()
                .map(s -> convertToDTO(s, true))
                .collect(Collectors.toList());
    }

    private ScreeningDTO convertToDTO(Screening screening, boolean includeDetails) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setMovieId(screening.getMovie().getId());
        dto.setHallId(screening.getHall().getId());
        dto.setStartTime(screening.getStartTime());
        dto.setPrice(screening.getPrice());
        dto.setAvailableSeats(screening.getAvailableSeats());

        if (includeDetails) {
            dto.setMovie(movieService.getMovieById(screening.getMovie().getId()));
            dto.setHall(hallService.getHallById(screening.getHall().getId()));
        }

        return dto;
    }
}

