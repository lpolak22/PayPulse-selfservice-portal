package com.example.paypulse.service.impl;

import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.dto.common.PageQuery;
import com.example.paypulse.dto.common.PageResponse;
import com.example.paypulse.dto.terminal.TerminalFilter;
import com.example.paypulse.dto.terminal.TerminalResponse;
import com.example.paypulse.dto.terminal.TerminalSearchCriteria;
import com.example.paypulse.dto.terminal.TerminalSort;
import com.example.paypulse.dto.terminal.TerminalStatus;
import com.example.paypulse.dto.terminal.TerminalType;
import com.example.paypulse.exception.FieldValidationException;
import com.example.paypulse.mapper.TerminalMapper;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.repository.TerminalSpecifications;
import com.example.paypulse.security.CurrentOrganisationScope;
import com.example.paypulse.service.TerminalService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private final TerminalMapper terminalMapper;
    private final CurrentOrganisationScope organisationScope;

    public TerminalServiceImpl(TerminalRepository terminalRepository,
                               TerminalMapper terminalMapper,
                               CurrentOrganisationScope organisationScope) {
        this.terminalRepository = terminalRepository;
        this.terminalMapper = terminalMapper;
        this.organisationScope = organisationScope;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TerminalResponse> getTerminals(TerminalSearchCriteria criteria, TerminalSort sort,
                                                       PageQuery pageQuery) {
        TerminalFilter filter = toFilter(criteria);
        Page<Terminal> terminals = terminalRepository.findAll(
                TerminalSpecifications.matching(organisationScope.terminals(), filter),
                pageQuery.toPageable(sort.toSort()));
        return PageResponse.from(terminals, terminalMapper::toResponse);
    }

    static TerminalFilter toFilter(TerminalSearchCriteria criteria) {
        List<ErrorResponse.FieldError> errors = new ArrayList<>();
        TerminalStatus status = null;
        TerminalType type = null;
        if (criteria.hasStatus()) {
            status = TerminalStatus.fromParam(criteria.status()).orElse(null);
            addErrorIfMissing(errors, status, "status", TerminalStatus.MESSAGE);
        }
        if (criteria.hasType()) {
            type = TerminalType.fromParam(criteria.type()).orElse(null);
            addErrorIfMissing(errors, type, "type", TerminalType.MESSAGE);
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
        return new TerminalFilter(status, type);
    }

    private static void addErrorIfMissing(List<ErrorResponse.FieldError> errors, Object value, String field,
                                          String message) {
        if (value == null) {
            errors.add(new ErrorResponse.FieldError(field, message));
        }
    }
}
