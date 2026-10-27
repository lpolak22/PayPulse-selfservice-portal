package com.example.paypulse.model;

public enum AuditAction {
    USER_CREATED("USER"),
    USER_UPDATED("USER"),
    USER_DELETED("USER");

    private final String entityType;

    AuditAction(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }
}
