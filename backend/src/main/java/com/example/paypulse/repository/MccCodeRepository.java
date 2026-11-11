package com.example.paypulse.repository;

import com.example.paypulse.model.MccCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MccCodeRepository extends JpaRepository<MccCode, String> {
}
