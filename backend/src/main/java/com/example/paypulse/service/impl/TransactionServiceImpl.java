package com.example.paypulse.service.impl;

import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.transaction.TransactionResponse;
import com.example.paypulse.dto.transaction.TransactionSearchCriteria;
import com.example.paypulse.dto.transaction.TransactionSort;
import com.example.paypulse.mapper.TransactionMapper;
import com.example.paypulse.repository.transaction.TransactionSearchQuery;
import com.example.paypulse.repository.transaction.TransactionSearchRepository;
import com.example.paypulse.security.CurrentOrganisationScope;
import com.example.paypulse.service.TransactionRange;
import com.example.paypulse.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionSearchRepository searchRepository;
    private final TransactionRangeResolver rangeResolver;
    private final TransactionMapper mapper;
    private final CurrentOrganisationScope organisationScope;

    public TransactionServiceImpl(TransactionSearchRepository searchRepository,
                                  TransactionRangeResolver rangeResolver,
                                  TransactionMapper mapper,
                                  CurrentOrganisationScope organisationScope) {
        this.searchRepository = searchRepository;
        this.rangeResolver = rangeResolver;
        this.mapper = mapper;
        this.organisationScope = organisationScope;
    }

    @Override
    public PageResponse<TransactionResponse> search(TransactionSearchCriteria criteria, TransactionSort sort,
                                                    PageQuery pageQuery) {
        TransactionRange range = rangeResolver.resolve(criteria.from(), criteria.to());
        TransactionSearchQuery query = toQuery(criteria, range, sort, pageQuery);
        long total = searchRepository.count(query);
        List<TransactionResponse> items = query.offset() >= total
                ? List.of()
                : searchRepository.findPage(query).stream().map(mapper::toResponse).toList();
        return new PageResponse<>(items, pageQuery.page(), pageQuery.pageSize(), total);
    }

    private TransactionSearchQuery toQuery(TransactionSearchCriteria criteria, TransactionRange range,
                                           TransactionSort sort, PageQuery pageQuery) {
        return new TransactionSearchQuery(
                organisationScope.organisationId(),
                range.start(),
                range.end(),
                criteria.statuses(),
                criteria.types(),
                criteria.cardBrands(),
                criteria.salesChannels(),
                criteria.authCode(),
                criteria.rrn(),
                criteria.outletId(),
                criteria.terminalId(),
                sort.field(),
                sort.direction(),
                pageQuery.pageSize(),
                (long) pageQuery.page() * pageQuery.pageSize()
        );
    }
}
