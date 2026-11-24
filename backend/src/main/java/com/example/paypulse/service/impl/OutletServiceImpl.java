package com.example.paypulse.service.impl;

import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.outlet.OutletResponse;
import com.example.paypulse.dto.outlet.OutletSearchCriteria;
import com.example.paypulse.dto.outlet.OutletStatus;
import com.example.paypulse.exception.FieldValidationException;
import com.example.paypulse.exception.ResourceNotFoundException;
import com.example.paypulse.mapper.OutletMapper;
import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.repository.MerchantRepository;
import com.example.paypulse.repository.OutletRepository;
import com.example.paypulse.repository.OutletSpecifications;
import com.example.paypulse.repository.OutletTerminalCount;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.security.CurrentOrganisationScope;
import com.example.paypulse.service.OutletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Service
public class OutletServiceImpl implements OutletService {

    private static final Sort BY_NAME = Sort.by("name", "idOutlet");
    private static final Locale CROATIAN = Locale.forLanguageTag("hr");
    private static final Comparator<String> PREFER_MIXED_CASE = Comparator
            .comparing((String city) -> city.equals(city.toUpperCase(CROATIAN)))
            .thenComparing(Comparator.naturalOrder());

    private final MerchantRepository merchantRepository;
    private final OutletRepository outletRepository;
    private final TerminalRepository terminalRepository;
    private final OutletMapper outletMapper;
    private final CurrentOrganisationScope organisationScope;

    public OutletServiceImpl(MerchantRepository merchantRepository,
                             OutletRepository outletRepository,
                             TerminalRepository terminalRepository,
                             OutletMapper outletMapper,
                             CurrentOrganisationScope organisationScope) {
        this.merchantRepository = merchantRepository;
        this.outletRepository = outletRepository;
        this.terminalRepository = terminalRepository;
        this.outletMapper = outletMapper;
        this.organisationScope = organisationScope;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutletResponse> getOutlets(Long merchantId, OutletSearchCriteria criteria,
                                                   PageQuery pageQuery) {
        validateStatus(criteria);
        requireVisibleMerchant(merchantId);
        Page<Outlet> outlets = outletRepository.findAll(
                OutletSpecifications.matching(organisationScope.outlets(), merchantId, criteria),
                pageQuery.toPageable(BY_NAME));
        Map<Long, Long> terminalCounts = terminalCounts(outlets.map(Outlet::getIdOutlet).getContent());
        return PageResponse.from(outlets, outlet -> outletMapper.toResponse(outlet,
                terminalCounts.getOrDefault(outlet.getIdOutlet(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCities(Long merchantId) {
        requireVisibleMerchant(merchantId);
        Map<String, String> citiesByName = outletRepository.findCitySpellingsByMerchantId(merchantId).stream()
                .collect(Collectors.toMap(city -> city.toLowerCase(CROATIAN), city -> city,
                        BinaryOperator.minBy(PREFER_MIXED_CASE), () -> new TreeMap<>(Collator.getInstance(CROATIAN))));
        return List.copyOf(citiesByName.values());
    }

    private static void validateStatus(OutletSearchCriteria criteria) {
        if (criteria.hasStatus() && !OutletStatus.isValid(criteria.status().strip())) {
            throw new FieldValidationException(List.of(new ErrorResponse.FieldError("status", OutletStatus.MESSAGE)));
        }
    }

    private void requireVisibleMerchant(Long merchantId) {
        Specification<Merchant> visible = organisationScope.merchants()
                .and((root, query, builder) -> builder.equal(root.get("idMerchant"), merchantId));
        if (!merchantRepository.exists(visible)) {
            throw new ResourceNotFoundException("Merchant", merchantId);
        }
    }

    private Map<Long, Long> terminalCounts(List<Long> outletIds) {
        if (outletIds.isEmpty()) {
            return Map.of();
        }
        return terminalRepository.countByOutletIds(outletIds).stream()
                .collect(Collectors.toMap(OutletTerminalCount::outletId, OutletTerminalCount::count));
    }
}
