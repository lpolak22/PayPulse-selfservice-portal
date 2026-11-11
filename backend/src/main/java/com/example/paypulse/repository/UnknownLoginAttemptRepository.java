package com.example.paypulse.repository;

import com.example.paypulse.model.UnknownLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UnknownLoginAttemptRepository extends JpaRepository<UnknownLoginAttempt, String> {

    @Modifying
    @Query(value = """
            insert into unknown_login_attempt (email_hash) values (:emailHash)
            on conflict (email_hash) do nothing""", nativeQuery = true)
    void insertIfMissing(String emailHash);

    @Modifying
    @Query("delete from UnknownLoginAttempt a where a.updatedAt < :cutoff")
    int deleteNotUpdatedSince(LocalDateTime cutoff);
}
