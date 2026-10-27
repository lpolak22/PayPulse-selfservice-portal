package com.example.paypulse.service.impl;

import com.example.paypulse.model.AuditAction;
import com.example.paypulse.model.AuditLog;
import com.example.paypulse.repository.AuditLogRepository;
import com.example.paypulse.security.CurrentUserProvider;
import com.example.paypulse.service.AuditLogService;
import com.example.paypulse.web.ClientIpAddressResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ClientIpAddressResolver clientIpAddressResolver;
    private final JsonMapper jsonMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository,
                               CurrentUserProvider currentUserProvider,
                               ClientIpAddressResolver clientIpAddressResolver,
                               JsonMapper jsonMapper) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserProvider = currentUserProvider;
        this.clientIpAddressResolver = clientIpAddressResolver;
        this.jsonMapper = jsonMapper;
    }

    // Joins the caller's transaction, so the audit row and the change it describes commit together.
    @Override
    @Transactional
    public void record(AuditAction action, Long entityId, Object oldValue, Object newValue) {
        AuditLog entry = new AuditLog(
                currentUserProvider.getCurrentUser().userId(),
                action,
                entityId,
                toJson(oldValue),
                toJson(newValue),
                clientIpAddressResolver.resolve().orElse(null)
        );
        auditLogRepository.save(entry);
    }

    private String toJson(Object value) {
        return value == null ? null : jsonMapper.writeValueAsString(value);
    }
}
