package com.example.paypulse.repository;

import com.example.paypulse.model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long>, JpaSpecificationExecutor<Terminal> {
    Optional<Terminal> findBySourceId(String sourceId);

    boolean existsByTerminalId(String terminalId);

    boolean existsBySourceId(String sourceId);

    @Query("SELECT new com.example.paypulse.repository.TerminalCount(t.outlet.merchant.idMerchant, COUNT(t)) "
            + "FROM Terminal t WHERE t.outlet.merchant.idMerchant IN :merchantIds GROUP BY t.outlet.merchant.idMerchant")
    List<TerminalCount> countByMerchantIds(@Param("merchantIds") Collection<Long> merchantIds);
}
