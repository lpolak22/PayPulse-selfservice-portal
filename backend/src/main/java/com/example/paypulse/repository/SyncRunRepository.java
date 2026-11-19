package com.example.paypulse.repository;

import com.example.paypulse.model.SyncRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SyncRunRepository extends JpaRepository<SyncRun, Long> {

    Optional<SyncRun> findFirstByStatusInOrderByStartedAtDesc(Collection<String> statuses);
}
