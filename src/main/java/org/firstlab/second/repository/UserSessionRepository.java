package org.firstlab.second.repository;

import org.firstlab.second.entity.SessionStatus;
import org.firstlab.second.entity.UserAccount;
import org.firstlab.second.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    Optional<UserSession> findByRefreshTokenHashAndStatus(String refreshTokenHash, SessionStatus status);

    List<UserSession> findByUserAndStatus(UserAccount user, SessionStatus status);

    List<UserSession> findByUser(UserAccount user);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = :status WHERE s.user = :user AND s.status = 'ACTIVE'")
    int revokeAllActiveSessionsForUser(@Param("user") UserAccount user, @Param("status") SessionStatus status);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'EXPIRED' WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    int markExpiredSessions(@Param("now") Instant now);

    long countByUserAndStatus(UserAccount user, SessionStatus status);
}

