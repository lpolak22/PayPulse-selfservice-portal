package com.example.paypulse.repository;

import com.example.paypulse.model.Terminal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long>, JpaSpecificationExecutor<Terminal> {
    Optional<Terminal> findBySourceId(String sourceId);

    @Query("SELECT t FROM Terminal t WHERE t.outlet.merchant.organisation.idOrganisation = :organisationId")
    List<Terminal> findAllInOrganisation(@Param("organisationId") Long organisationId);

    boolean existsByTerminalId(String terminalId);

    boolean existsBySourceId(String sourceId);

    @Override
    @EntityGraph(attributePaths = {"outlet", "outlet.merchant"})
    Page<Terminal> findAll(Specification<Terminal> specification, Pageable pageable);

    @Query("SELECT new com.example.paypulse.repository.TerminalCount(t.outlet.merchant.idMerchant, COUNT(t)) "
            + "FROM Terminal t WHERE t.outlet.merchant.idMerchant IN :merchantIds GROUP BY t.outlet.merchant.idMerchant")
    List<TerminalCount> countByMerchantIds(@Param("merchantIds") Collection<Long> merchantIds);

    @Query("SELECT new com.example.paypulse.repository.OutletTerminalCount(t.outlet.idOutlet, COUNT(t)) "
            + "FROM Terminal t WHERE t.outlet.idOutlet IN :outletIds GROUP BY t.outlet.idOutlet")
    List<OutletTerminalCount> countByOutletIds(@Param("outletIds") Collection<Long> outletIds);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            delete from terminal t
            using outlet o, merchant m
            where t.id_outlet = o.id_outlet
              and o.id_merchant = m.id_merchant
              and m.id_organisation = :organisationId
              and t.source_id in (:sourceIds)
              and not exists (select 1 from transaction x where x.id_terminal = t.id_terminal)
            """, nativeQuery = true)
    int deleteWithoutTransactions(@Param("organisationId") Long organisationId,
                                  @Param("sourceIds") Collection<String> sourceIds);
}
