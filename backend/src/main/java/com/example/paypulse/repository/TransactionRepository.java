package com.example.paypulse.repository;

import com.example.paypulse.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("select t.sourceId from Transaction t where t.sourceId like concat(:prefix, '%')")
    List<String> findSourceIdsByPrefix(@Param("prefix") String prefix);

    boolean existsBySourceId(String sourceId);
}
