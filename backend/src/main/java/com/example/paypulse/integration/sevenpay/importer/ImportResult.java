package com.example.paypulse.integration.sevenpay.importer;

public record ImportResult(String entity, int created, int updated) {

    @Override
    public String toString() {
        return entity + ": " + created + " created, " + updated + " updated";
    }
}
