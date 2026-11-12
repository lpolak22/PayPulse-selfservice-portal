package com.example.paypulse.repository;

import com.example.paypulse.model.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutletRepository extends JpaRepository<Outlet, Long>, JpaSpecificationExecutor<Outlet> {
    Optional<Outlet> findBySourceId(String sourceId);

    boolean existsBySourceId(String sourceId);

    @Query("SELECT new com.example.paypulse.repository.OutletCount(o.merchant.idMerchant, COUNT(o)) "
            + "FROM Outlet o WHERE o.merchant.idMerchant IN :merchantIds GROUP BY o.merchant.idMerchant")
    List<OutletCount> countByMerchantIds(@Param("merchantIds") Collection<Long> merchantIds);
}
