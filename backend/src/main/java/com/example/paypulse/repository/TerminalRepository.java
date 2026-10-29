package com.example.paypulse.repository;

import com.example.paypulse.model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    boolean existsByTerminalId(String terminalId);

    boolean existsBySourceId(String sourceId);
}
