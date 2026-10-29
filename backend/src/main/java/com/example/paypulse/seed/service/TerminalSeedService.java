package com.example.paypulse.seed.service;

import com.example.paypulse.model.MccCode;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.seed.util.DataGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class TerminalSeedService {
    private final TerminalRepository terminalRepository;
    private final DataGenerator dataGenerator;

    public TerminalSeedService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
        this.dataGenerator = new DataGenerator(new Random(54321));
    }

    @Transactional
    public List<Terminal> createTerminals(List<Outlet> outlets, List<MccCode> mccCodes) {
        List<Terminal> terminals = new ArrayList<>();
        Random rand = new Random(54321);
        String[] terminalTypes = {"POS", "MOBILE", "ECR", "VIRTUAL"};
        String[] terminalStatuses = {"ACTIVE", "ACTIVE", "ACTIVE", "INACTIVE", "DEACTIVATED"};

        for (Outlet outlet : outlets) {
            int terminalCount = 2 + rand.nextInt(3); // 2 to 4
            for (int i = 0; i < terminalCount; i++) {
                String termSourceId = "TRM-" + outlet.getSourceId() + "-" + i;
                String termId = dataGenerator.randomTerminalId();
                if (terminalRepository.existsByTerminalId(termId) || terminalRepository.existsBySourceId(termSourceId)) {
                    terminalRepository.findAll().stream()
                            .filter(t -> termSourceId.equals(t.getSourceId()) || termId.equals(t.getTerminalId()))
                            .findFirst()
                            .ifPresent(terminals::add);
                    continue;
                }
                Terminal terminal = new Terminal();
                terminal.setOutlet(outlet);
                terminal.setMccCode(mccCodes.get(rand.nextInt(mccCodes.size())));
                terminal.setSourceId(termSourceId);
                terminal.setTerminalId(termId);
                terminal.setType(terminalTypes[rand.nextInt(terminalTypes.length)]);
                String status = terminalStatuses[rand.nextInt(terminalStatuses.length)];
                terminal.setStatus(status);
                if ("ACTIVE".equals(status)) {
                    terminal.setActivatedAt(LocalDateTime.now().minusMonths(rand.nextInt(12) + 1));
                    terminal.setDeactivatedAt(null);
                } else if ("DEACTIVATED".equals(status)) {
                    terminal.setActivatedAt(LocalDateTime.now().minusMonths(rand.nextInt(18) + 6));
                    terminal.setDeactivatedAt(LocalDateTime.now().minusMonths(rand.nextInt(3)));
                } else {
                    terminal.setActivatedAt(LocalDateTime.now().minusMonths(rand.nextInt(6)));
                    terminal.setDeactivatedAt(null);
                }
                terminal = terminalRepository.save(terminal);
                terminals.add(terminal);
            }
        }
        return terminals;
    }
}
