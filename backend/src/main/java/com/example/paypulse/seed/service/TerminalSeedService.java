package com.example.paypulse.seed.service;

import com.example.paypulse.model.MccCode;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.model.Terminal;
import com.example.paypulse.repository.TerminalRepository;
import com.example.paypulse.seed.util.DataGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Profile("seed")
public class TerminalSeedService {
    private final TerminalRepository terminalRepository;
    private final Random random = new Random(54321);
    private final DataGenerator dataGenerator = new DataGenerator(random);

    public TerminalSeedService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    @Transactional
    public List<Terminal> createTerminals(List<Outlet> outlets, List<MccCode> mccCodes) {
        List<Terminal> terminals = new ArrayList<>();
        String[] terminalTypes = {"POS", "MOBILE", "ECR", "VIRTUAL"};
        String[] terminalStatuses = {"ACTIVE", "ACTIVE", "ACTIVE", "INACTIVE", "DEACTIVATED"};

        for (Outlet outlet : outlets) {
            int terminalCount = 2 + random.nextInt(3); // 2 to 4
            for (int i = 0; i < terminalCount; i++) {
                String termSourceId = "TRM-" + outlet.getSourceId() + "-" + i;
                Optional<Terminal> existing = terminalRepository.findBySourceId(termSourceId);
                if (existing.isPresent()) {
                    terminals.add(existing.get());
                    continue;
                }
                String termId = dataGenerator.randomTerminalId();
                while (terminalRepository.existsByTerminalId(termId)) {
                    termId = dataGenerator.randomTerminalId();
                }
                Terminal terminal = new Terminal();
                terminal.setOutlet(outlet);
                terminal.setMccCode(mccCodes.get(random.nextInt(mccCodes.size())));
                terminal.setSourceId(termSourceId);
                terminal.setTerminalId(termId);
                terminal.setType(terminalTypes[random.nextInt(terminalTypes.length)]);
                String status = terminalStatuses[random.nextInt(terminalStatuses.length)];
                terminal.setStatus(status);
                if ("ACTIVE".equals(status)) {
                    terminal.setActivatedAt(LocalDateTime.now().minusMonths(random.nextInt(12) + 1));
                    terminal.setDeactivatedAt(null);
                } else if ("DEACTIVATED".equals(status)) {
                    terminal.setActivatedAt(LocalDateTime.now().minusMonths(random.nextInt(18) + 6));
                    terminal.setDeactivatedAt(LocalDateTime.now().minusMonths(random.nextInt(3)));
                } else {
                    terminal.setActivatedAt(LocalDateTime.now().minusMonths(random.nextInt(6)));
                    terminal.setDeactivatedAt(null);
                }
                terminal = terminalRepository.save(terminal);
                terminals.add(terminal);
            }
        }
        return terminals;
    }
}
