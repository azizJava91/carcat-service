package com.carland.carland_service.enums;

import lombok.Getter;

import java.util.Arrays;


@Getter
public enum ColorTranslation {

    BEIGE("Beige", "Bej"),
    BLACK("Black", "Qara"),
    BLUE("Blue", "Mavi"),
    BROWN("Brown", "Qəhvəyi"),
    GOLD("Gold", "Qızıl"),
    GRAY("Gray / Grey", "Boz"),
    GREEN("Green", "Yaşıl"),
    MAROON("Maroon", "Bordo"),
    MATTE_BLACK("Matte Black", "Mat qara"),
    METALLIC_SILVER("Metallic silver", "Metalik gümüş"),
    NAVY_BLUE("Navy blue", "Tünd mavi"),
    ORANGE("Orange", "Narıncı"),
    PEARL_WHITE("Pearl white", "İncə ağ"),
    PURPLE("Purple", "Bənövşəyi"),
    RED("Red", "Qırmızı"),
    SILVER("Silver", "Gümüş"),
    WHITE("White", "Ağ"),
    YELLOW("Yellow", "Sarı"),
    OTHER("Other", "Digər");

    private final String en;
    private final String az;

    ColorTranslation(String en, String az) {
        this.en = en;
        this.az = az;
    }

    public static String translate(String enValue, String lang) {
        if (!"az".equalsIgnoreCase(lang)) {
            return enValue;
        }

        return Arrays.stream(values())
                .filter(c -> c.en.equalsIgnoreCase(enValue))
                .map(ColorTranslation::getAz)
                .findFirst()
                .orElse(enValue);
    }
}



