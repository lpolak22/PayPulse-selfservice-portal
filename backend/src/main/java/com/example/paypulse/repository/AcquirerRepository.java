package com.example.paypulse.repository;

import com.example.paypulse.model.Acquirer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcquirerRepository extends JpaRepository<Acquirer, Long> {
    Optional<Acquirer> findByCode(String code);

    boolean existsByCode(String code);
}
