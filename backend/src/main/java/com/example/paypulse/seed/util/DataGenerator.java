package com.example.paypulse.seed.util;

import java.util.Random;

public class DataGenerator {
    private final Random random;

    public DataGenerator(Random random) {
        this.random = random;
    }

    public String randomCroatianCity() {
        String[] cities = {"Zagreb", "Split", "Rijeka", "Osijek", "Zadar", "Dubrovnik", "Pula", "Šibenik", "Karlovac"};
        return cities[random.nextInt(cities.length)];
    }

    public String randomCroatianStreet() {
        String[] streets = {"Ilica", "Savska cesta", "Vukovarska ulica", "Poljička cesta", "Splitska ulica", "Riječka ulica", "Zagrebačka ulica", "Dubrovačka ulica"};
        return streets[random.nextInt(streets.length)];
    }

    public String randomPostalCode() {
        return String.format("%05d", random.nextInt(100000));
    }

    public String randomPhone() {
        return "+385" + (random.nextInt(900000000) + 100000000);
    }

    public String randomEmail(String prefix) {
        return prefix.toLowerCase().replace(" ", "") + random.nextInt(10000) + "@example.hr";
    }

    public String randomTerminalId() {
        return "T" + String.format("%015d", Math.abs(random.nextLong())).substring(0, 15);
    }

    public String randomAuthCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    public String randomRrn() {
        return String.format("%012d", Math.abs(random.nextLong() % 1000000000000L));
    }

    public String randomMaskedPan(String brand) {
        String pan;
        if ("AMEX".equals(brand)) {
            pan = "34" + String.format("%013d", Math.abs(random.nextLong() % 10000000000000L));
            return pan.substring(0, 6) + "*****" + pan.substring(pan.length() - 4);
        } else {
            pan = "4".equals(brand) || random.nextBoolean() ? "4" + String.format("%015d", Math.abs(random.nextLong() % 1000000000000000L))
                    : "5" + String.format("%015d", Math.abs(random.nextLong() % 1000000000000000L));
            return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
        }
    }
}
