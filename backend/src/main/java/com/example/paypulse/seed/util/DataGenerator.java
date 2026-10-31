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
        return switch (brand) {
            case "AMEX" -> mask("34" + randomDigits(13));
            case "DINERS" -> mask("36" + randomDigits(12));
            case "MASTERCARD" -> mask("5" + randomDigits(15));
            case "MAESTRO" -> mask("6" + randomDigits(15));
            default -> mask("4" + randomDigits(15));
        };
    }

    private String mask(String pan) {
        int hidden = pan.length() - 10;
        return pan.substring(0, 6) + "*".repeat(hidden) + pan.substring(pan.length() - 4);
    }

    private String randomDigits(int count) {
        StringBuilder digits = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            digits.append(random.nextInt(10));
        }
        return digits.toString();
    }
}
