package com.example.paypulse.service.impl;

import com.example.paypulse.dto.ValidationPatterns;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.dto.merchant.MerchantSearchCriteria;
import com.example.paypulse.dto.merchant.MerchantSort;
import com.example.paypulse.dto.merchant.MerchantStatus;
import com.example.paypulse.exception.FieldValidationException;
import com.example.paypulse.mapper.MerchantMapper;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.repository.MerchantRepository;
import com.example.paypulse.repository.MerchantSpecifications;
import com.example.paypulse.repository.OutletCount;
import com.example.paypulse.repository.OutletRepository;
import com.example.paypulse.repository.TerminalCount;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.security.CurrentOrganisationScope;
import com.example.paypulse.service.MerchantService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final OutletRepository outletRepository;
    private final TerminalRepository terminalRepository;
    private final MerchantMapper merchantMapper;
    private final CurrentOrganisationScope organisationScope;

    public MerchantServiceImpl(MerchantRepository merchantRepository,
                               OutletRepository outletRepository,
                               TerminalRepository terminalRepository,
                               MerchantMapper merchantMapper,
                               CurrentOrganisationScope organisationScope) {
        this.merchantRepository = merchantRepository;
        this.outletRepository = outletRepository;
        this.terminalRepository = terminalRepository;
        this.merchantMapper = merchantMapper;
        this.organisationScope = organisationScope;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MerchantResponse> getMerchants(MerchantSearchCriteria criteria, MerchantSort sort,
                                                       PageQuery pageQuery) {
        validateCriteria(criteria);
        Page<Merchant> merchants = merchantRepository.findAll(
                MerchantSpecifications.matching(organisationScope.merchants(), criteria),
                pageQuery.toPageable(sort.toSort()));
        List<Long> merchantIds = merchants.map(Merchant::getIdMerchant).getContent();
        Map<Long, Long> outletCounts = outletCounts(merchantIds);
        Map<Long, Long> terminalCounts = terminalCounts(merchantIds);
        return PageResponse.from(merchants, merchant -> merchantMapper.toResponse(merchant,
                outletCounts.getOrDefault(merchant.getIdMerchant(), 0L),
                terminalCounts.getOrDefault(merchant.getIdMerchant(), 0L)));
    }

    private Map<Long, Long> outletCounts(List<Long> merchantIds) {
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return outletRepository.countByMerchantIds(merchantIds).stream()
                .collect(Collectors.toMap(OutletCount::merchantId, OutletCount::count));
    }

    private Map<Long, Long> terminalCounts(List<Long> merchantIds) {
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return terminalRepository.countByMerchantIds(merchantIds).stream()
                .collect(Collectors.toMap(TerminalCount::merchantId, TerminalCount::count));
    }

    private static void validateCriteria(MerchantSearchCriteria criteria) {
        List<ErrorResponse.FieldError> errors = new ArrayList<>();
        if (criteria.hasOib() && !criteria.oib().strip().matches(ValidationPatterns.OIB)) {
            errors.add(new ErrorResponse.FieldError("oib", ValidationPatterns.OIB_MESSAGE));
        }
        if (criteria.hasStatus() && !MerchantStatus.isValid(criteria.status().strip())) {
            errors.add(new ErrorResponse.FieldError("status", MerchantStatus.MESSAGE));
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
