package com.example.paypulse.service;

import com.example.paypulse.model.AuditAction;

public interface AuditLogService {

    /**
     * Records one audit entry on behalf of the current user.
     * The old and new values are stored as JSON snapshots; either may be null.
     */
    void record(AuditAction action, Long entityId, Object oldValue, Object newValue);
}
