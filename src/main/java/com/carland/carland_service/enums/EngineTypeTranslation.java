package com.carland.carland_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EngineTypeTranslation {

    PETROL_GASOLINE("Petrol (Gasoline)", "Benzin"),
    DIESEL("Diesel", "Dizel"),
    HYBRID("Hybrid", "Hibrid"),
    PLUG_IN_HYBRID("Plug-in Hybrid", "Plug-in Hibrid"),
    ELECTRIC("Electric", "Elektro"),
    GAS_LPG_CNG("Gas (LPG / CNG)", "Qaz"),
    HYDROGEN("Hydrogen", "Hidrogen"),
    DIESEL_HYBRID("Diesel Hybrid", "Dizel-Hibrid");

    private final String en;
    private final String az;

    public static String translate(String enValue, String acceptLanguage) {
        for (EngineTypeTranslation type : values()) {
            if (type.en.equalsIgnoreCase(enValue)) {
                return "az".equalsIgnoreCase(acceptLanguage)
                        ? type.az
                        : type.en;
            }
        }
        return enValue;
    }
}
