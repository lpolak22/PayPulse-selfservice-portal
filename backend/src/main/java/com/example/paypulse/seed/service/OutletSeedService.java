package com.example.paypulse.seed.service;

import com.example.paypulse.model.Merchant;
import com.example.paypulse.model.Outlet;
import com.example.paypulse.repository.OutletRepository;
import com.example.paypulse.seed.util.DataGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OutletSeedService {
    private final OutletRepository outletRepository;
    private final DataGenerator dataGenerator;

    public OutletSeedService(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
        this.dataGenerator = new DataGenerator(new Random(12345));
    }

    @Transactional
    public List<Outlet> createOutlets(List<Merchant> merchants) {
        List<Outlet> outlets = new ArrayList<>();
        Random rand = new Random(12345);
        for (Merchant merchant : merchants) {
            int outletCount = 2 + rand.nextInt(4); // 2 to 5
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
                outlet.setAddress(street + " " + (rand.nextInt(200) + 1) + ", " + city);
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
