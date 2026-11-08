package com.example.paypulse.service.impl;

import com.example.paypulse.dto.ValidationPatterns;
import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.merchant.MerchantResponse;
import com.example.paypulse.dto.merchant.MerchantSearchCriteria;
import com.example.paypulse.exception.FieldValidationException;
import com.example.paypulse.mapper.MerchantMapper;
import com.example.paypulse.repository.MerchantRepository;
import com.example.paypulse.repository.MerchantSpecifications;
import com.example.paypulse.security.CurrentOrganisationScope;
import com.example.paypulse.service.MerchantService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MerchantServiceImpl implements MerchantService {

    private static final Sort DEFAULT_SORT = Sort.by("name", "idMerchant");

    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final CurrentOrganisationScope organisationScope;

    public MerchantServiceImpl(MerchantRepository merchantRepository,
                               MerchantMapper merchantMapper,
                               CurrentOrganisationScope organisationScope) {
        this.merchantRepository = merchantRepository;
        this.merchantMapper = merchantMapper;
        this.organisationScope = organisationScope;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MerchantResponse> getMerchants(MerchantSearchCriteria criteria, PageQuery pageQuery) {
        validateOib(criteria);
        return PageResponse.from(
                merchantRepository.findAll(MerchantSpecifications.matching(organisationScope.merchants(), criteria),
                        pageQuery.toPageable(DEFAULT_SORT)),
                merchantMapper::toResponse);
    }

    private static void validateOib(MerchantSearchCriteria criteria) {
        if (criteria.hasOib() && !criteria.oib().strip().matches(ValidationPatterns.OIB)) {
            throw new FieldValidationException(
                    List.of(new ErrorResponse.FieldError("oib", ValidationPatterns.OIB_MESSAGE)));
        }
    }
}
