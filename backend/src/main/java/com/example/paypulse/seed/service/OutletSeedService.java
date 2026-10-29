package com.example.paypulse.seed.service;

import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.repository.OutletRepository;
import com.example.paypulse.seed.util.DataGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Profile("seed")
public class OutletSeedService {
    private final OutletRepository outletRepository;
    private final Random random = new Random(12345);
    private final DataGenerator dataGenerator = new DataGenerator(random);

    public OutletSeedService(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
    }

    @Transactional
    public List<Outlet> createOutlets(List<Merchant> merchants) {
        List<Outlet> outlets = new ArrayList<>();
        for (Merchant merchant : merchants) {
            int outletCount = 2 + random.nextInt(4);
            for (int i = 0; i < outletCount; i++) {
                String sourceId = "OUT-" + merchant.getSourceId() + "-" + i;
                if (outletRepository.existsBySourceId(sourceId)) {
                    outletRepository.findAll().stream()
                            .filter(o -> sourceId.equals(o.getSourceId()))
                            .findFirst()
                            .ifPresent(outlets::add);
                    continue;
                }
                Outlet outlet = new Outlet();
                outlet.setMerchant(merchant);
                outlet.setSourceId(sourceId);
                outlet.setName(merchant.getName() + " - Outlet " + (i + 1));
                String city = dataGenerator.randomCroatianCity();
                String street = dataGenerator.randomCroatianStreet();
                outlet.setAddress(street + " " + (random.nextInt(200) + 1) + ", " + city);
                outlet.setCity(city);
                outlet.setPostalCode(dataGenerator.randomPostalCode());
                outlet.setPhone(dataGenerator.randomPhone());
                outlet.setEmail("outlet" + i + "." + merchant.getSourceId().toLowerCase() + "@example.hr");
                outlet.setStatus("ACTIVE");
                outlet.setIsActive(true);
                outlet = outletRepository.save(outlet);
                outlets.add(outlet);
            }
        }
        return outlets;
    }
}
