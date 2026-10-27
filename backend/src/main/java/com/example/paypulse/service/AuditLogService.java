package com.example.paypulse.service;

import com.example.paypulse.model.AuditAction;

public interface AuditLogService {

    void record(AuditAction action, Long entityId, Object oldValue, Object newValue);
}
