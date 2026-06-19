package com.carland.carland_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BodyTypeTranslation {

    SEDAN("Sedan", "Sedan"),
    SUV("SUV", "SUV / Yolsuzluq"),
    HATCHBACK("Hatchback", "Hetçbek"),
    CROSSOVER("Crossover", "Krossover"),
    WAGON("Wagon / Estate", "Universal"),
    PICKUP("Pickup", "Pikap"),
    MINIVAN("Minivan", "Minivan"),
    COUPE("Coupe", "Kupe"),
    LIFTBACK("Liftback", "Liftbek"),
    VAN("Van", "Mikroavtobus"),
    CONVERTIBLE("Convertible", "Kabriolet"),
    OTHER("Other", "Digər");

    private final String en;
    private final String az;

    public static String translate(String enValue, String acceptLanguage) {
        for (BodyTypeTranslation type : values()) {
            if (type.en.equalsIgnoreCase(enValue)) {
                return "az".equalsIgnoreCase(acceptLanguage)
                        ? type.az
                        : type.en;
            }
        }
        return enValue;
    }
}
