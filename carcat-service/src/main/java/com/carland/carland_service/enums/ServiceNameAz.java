package com.carland.carland_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ServiceNameAz {

    ENGINE_OIL("Engine oil and oil filter", "Mühərrik yağı və yağ filteri"),
    AIR_FILTER("Air filter", "Hava filteri"),
    CABIN_FILTER("Cabin filter", "Salon filteri"),
    FUEL_FILTER("Fuel filter", "Yanacaq filteri"),
    SPARK_PLUGS("Spark plugs", "Alışma şamları"),
    COOLANT("Coolant", "Soyuducu maye"),
    AT_FLUID("Automatic transmission fluid", "Avtomatik transmissiya yağı"),
    TIMING_BELT("Engine timing belt & kit", "Mühərrikin iç kəməri və dəsti"),
    FEAD("Auxiliary drive belt (FEAD)", "Çöl kəmər (FEAD)"),
    BRAKE_FLUID("Brake fluid", "Əyləc mayesi");

    private final String en;
    private final String az;

    public static String translate(String enValue, String lang) {
        if (!"az".equalsIgnoreCase(lang)) {
            return enValue;
        }

        return Arrays.stream(values())
                .filter(s -> s.en.equalsIgnoreCase(enValue))
                .map(ServiceNameAz::getAz)
                .findFirst()
                .orElse(enValue);
    }
}
