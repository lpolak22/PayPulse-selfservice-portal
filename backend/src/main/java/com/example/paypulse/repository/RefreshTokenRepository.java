package com.example.paypulse.repository;

import com.example.paypulse.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("""
            select t from RefreshToken t join fetch t.user u join fetch u.organisation
            where t.tokenHash = :tokenHash""")

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken t set t.usedAt = :now
            where t.user.idUser = :userId and t.usedAt is null""")

    int revokeAllForUser(Long userId, LocalDateTime now);

    @Modifying
    @Query("""
            delete from RefreshToken t
            where t.expiresAt < :cutoff or t.usedAt < :cutoff""")

    int deleteExpiredOrUsedBefore(LocalDateTime cutoff);
}
