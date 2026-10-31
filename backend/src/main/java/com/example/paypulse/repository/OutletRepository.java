package com.example.paypulse.repository;

import com.example.paypulse.model.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutletRepository extends JpaRepository<Outlet, Long>, JpaSpecificationExecutor<Outlet> {
    Optional<Outlet> findBySourceId(String sourceId);

    boolean existsBySourceId(String sourceId);
}
