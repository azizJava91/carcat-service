package com.carland.carland_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumFeedbackSuccessResponse {

    SUCCESS_MESSAGE(
            "Müraciətiniz qeydə alındı",
            "Your request has been received",
            "Ваш запрос был принят"
    ),
    ESTIMATED_TIME(
            "24–48 saat",
            "24–48 hours",
            "24–48 часов"
    );

    private final String azMessage;
    private final String enMessage;
    private final String ruMessage;

    public String getMessageByLang(String lang) {
        if (lang == null) return azMessage;

        return switch (lang.toLowerCase()) {
            case "en" -> enMessage;
            case "ru" -> ruMessage;
            default -> azMessage;
        };
    }
}
