package com.example.paypulse.integration.sevenpay.importer;

import com.example.paypulse.integration.sevenpay.SevenPayClient;
import com.example.paypulse.integration.sevenpay.SevenPayPages;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTerminal;
import com.example.paypulse.model.MccCode;
import com.example.paypulse.model.Organisation;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.repository.MccCodeRepository;
import com.example.paypulse.repository.OrganisationScope;
import com.example.paypulse.repository.OutletRepository;
import com.example.paypulse.repository.TerminalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SevenPayTerminalImporter {

    private static final Logger log = LoggerFactory.getLogger(SevenPayTerminalImporter.class);
    private static final String MCC = "^\\d{3,4}(-\\d{3,4})?$";
    private static final Set<String> SOFTPOS_TYPES = Set.of("softposv1", "softposv2");

    private final SevenPayClient sevenPayClient;
    private final OutletRepository outletRepository;
    private final TerminalRepository terminalRepository;
    private final MccCodeRepository mccCodeRepository;

    public SevenPayTerminalImporter(SevenPayClient sevenPayClient,
                                    OutletRepository outletRepository,
                                    TerminalRepository terminalRepository,
                                    MccCodeRepository mccCodeRepository) {
        this.sevenPayClient = sevenPayClient;
        this.outletRepository = outletRepository;
        this.terminalRepository = terminalRepository;
        this.mccCodeRepository = mccCodeRepository;
    }

    @Transactional
    public ImportResult importTerminals(Organisation organisation) {
        Long organisationId = organisation.getIdOrganisation();
        Map<String, Outlet> outletsByMid = outletRepository.findAll(OrganisationScope.outlets(organisationId))
                .stream()
                .collect(Collectors.toMap(Outlet::getSourceId, Function.identity()));
        Map<String, Terminal> terminalsByTid = terminalRepository.findAll(OrganisationScope.terminals(organisationId))
                .stream()
                .collect(Collectors.toMap(Terminal::getSourceId, Function.identity()));
        Map<String, MccCode> mccCodes = new HashMap<>();
        Map<Outcome, Integer> counts = new EnumMap<>(Outcome.class);
        Set<String> ignoredTids = new HashSet<>();
        for (SevenPayTerminal source : SevenPayPages.fetchAll(sevenPayClient::getTerminals)) {
            if (isSoftPos(source.terminalType())) {
                counts.merge(importTerminal(source, outletsByMid, terminalsByTid, mccCodes), 1, Integer::sum);
            } else {
                ignoredTids.add(source.tid());
            }
        }
        removeIgnoredTerminals(organisationId, ignoredTids);
        return new ImportResult("terminals", counts.getOrDefault(Outcome.CREATED, 0),
                counts.getOrDefault(Outcome.UPDATED, 0), counts.getOrDefault(Outcome.SKIPPED, 0));
    }

    private Outcome importTerminal(SevenPayTerminal source, Map<String, Outlet> outletsByMid,
                                   Map<String, Terminal> terminalsByTid, Map<String, MccCode> mccCodes) {
        Outlet outlet = outletsByMid.get(source.midReference());
        if (outlet == null) {
            log.warn("Skipping terminal {}: outlet {} is not imported", source.tid(), source.midReference());
            return Outcome.SKIPPED;
        }
        if (source.merchantCategoryCode() == null || !source.merchantCategoryCode().matches(MCC)) {
            log.warn("Skipping terminal {}: MCC {} is not a code or a range of codes",
                    source.tid(), source.merchantCategoryCode());
            return Outcome.SKIPPED;
        }
        Terminal existing = terminalsByTid.get(source.tid());
        Terminal terminal = existing == null ? new Terminal() : existing;
        apply(terminal, outlet, mccCodes.computeIfAbsent(source.merchantCategoryCode(), this::findOrCreateMcc), source);
        terminalRepository.save(terminal);
        return existing == null ? Outcome.CREATED : Outcome.UPDATED;
    }

    private void removeIgnoredTerminals(Long organisationId, Set<String> ignoredTids) {
        if (ignoredTids.isEmpty()) {
            return;
        }
        int removed = terminalRepository.deleteWithoutTransactions(organisationId, ignoredTids);
        log.info("Ignored {} 7Pay terminals that are not SoftPOS V1 or V2, removed {} earlier imported ones",
                ignoredTids.size(), removed);
    }

    static boolean isSoftPos(String terminalType) {
        return terminalType != null && SOFTPOS_TYPES.contains(normalise(terminalType));
    }

    private static String normalise(String terminalType) {
        return terminalType.replace(" ", "").toLowerCase(Locale.ROOT);
    }

    private MccCode findOrCreateMcc(String code) {
        return mccCodeRepository.findById(code)
                .orElseGet(() -> mccCodeRepository.save(new MccCode(code, describeMcc(code))));
    }

    private static String describeMcc(String code) {
        return code.contains("-") ? "MCC range " + code : "MCC " + code;
    }

    private static void apply(Terminal terminal, Outlet outlet, MccCode mccCode, SevenPayTerminal source) {
        String previousStatus = terminal.getStatus();
        String status = SevenPayStatus.toLocal(source.status());
        terminal.setOutlet(outlet);
        terminal.setMccCode(mccCode);
        terminal.setSourceId(source.tid());
        terminal.setTerminalId(source.tid());
        terminal.setType(source.terminalType());
        terminal.setStatus(status);
        if (previousStatus != null && !previousStatus.equals(status)) {
            if (SevenPayStatus.ACTIVE.equals(status)) {
                terminal.setActivatedAt(LocalDateTime.now());
                terminal.setDeactivatedAt(null);
            } else {
                terminal.setDeactivatedAt(LocalDateTime.now());
            }
        }
    }

    private enum Outcome { CREATED, UPDATED, SKIPPED }
}
