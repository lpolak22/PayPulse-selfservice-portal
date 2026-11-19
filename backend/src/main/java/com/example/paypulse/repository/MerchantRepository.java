package com.example.paypulse.repository;

import com.example.paypulse.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long>, JpaSpecificationExecutor<Merchant> {
    Optional<Merchant> findByRegistrationNumber(String registrationNumber);

    boolean existsBySourceId(String sourceId);

    @Query("SELECT m.registrationNumber FROM Merchant m WHERE m.organisation.idOrganisation = :organisationId "
            + "AND m.registrationNumber IS NOT NULL ORDER BY m.registrationNumber")
    List<String> findRegistrationNumbersInOrganisation(@Param("organisationId") Long organisationId);
}
