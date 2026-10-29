package com.example.paypulse.repository;

import com.example.paypulse.model.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutletRepository extends JpaRepository<Outlet, Long> {
    boolean existsBySourceId(String sourceId);
}
