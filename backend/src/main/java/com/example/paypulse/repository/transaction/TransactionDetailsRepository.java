package com.example.paypulse.repository.transaction;

import java.util.Optional;

public interface TransactionDetailsRepository {

    Optional<TransactionDetailsRow> findInOrganisation(long id, long organisationId);
}
