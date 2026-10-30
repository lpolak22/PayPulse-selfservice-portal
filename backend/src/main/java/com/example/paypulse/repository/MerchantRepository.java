package com.example.paypulse.repository;

import com.example.paypulse.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long>, JpaSpecificationExecutor<Merchant> {
    Optional<Merchant> findByRegistrationNumber(String registrationNumber);

    boolean existsBySourceId(String sourceId);
}
