package com.example.paypulse.repository;

import com.example.paypulse.model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long>, JpaSpecificationExecutor<Terminal> {
    Optional<Terminal> findBySourceId(String sourceId);

    boolean existsByTerminalId(String terminalId);

    boolean existsBySourceId(String sourceId);
}
