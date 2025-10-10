package org.firstlab.second.repository;

import org.firstlab.second.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    List<Screening> findByMovieId(Long movieId);
    List<Screening> findByHallId(Long hallId);
    List<Screening> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Screening s WHERE s.startTime > :now ORDER BY s.startTime")
    List<Screening> findUpcomingScreenings(@Param("now") LocalDateTime now);
}

