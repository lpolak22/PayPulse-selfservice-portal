package com.example.paypulse.repository;

import com.example.paypulse.dto.terminal.TerminalFilter;
import com.example.paypulse.dto.terminal.TerminalStatus;
import com.example.paypulse.dto.terminal.TerminalType;
import com.example.paypulse.model.Terminal;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TerminalSpecifications {

    private TerminalSpecifications() {
    }

    public static Specification<Terminal> matching(Specification<Terminal> scope, TerminalFilter filter) {
        List<Specification<Terminal>> specifications = new ArrayList<>();
        specifications.add(scope);
        if (filter.hasStatus()) {
            specifications.add(hasStatus(filter.status()));
        }
        if (filter.hasType()) {
            specifications.add(hasType(filter.type()));
        }
        if (filter.hasMerchant()) {
            specifications.add(belongsToMerchant(filter.merchantId()));
        }
        return Specification.allOf(specifications);
    }

    public static Specification<Terminal> belongsToMerchant(Long merchantId) {
        return (root, query, builder) -> builder.equal(
                root.get("outlet").get("merchant").get("idMerchant"), merchantId);
    }

    static Specification<Terminal> hasStatus(TerminalStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status.name());
    }

    static Specification<Terminal> hasType(TerminalType type) {
        return (root, query, builder) -> builder.equal(
                builder.lower(builder.function("replace", String.class,
                        root.get("type"), builder.literal(" "), builder.literal(""))),
                type.key());
    }
}
