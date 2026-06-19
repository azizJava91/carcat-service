package com.carland.carland_service.enums;

import lombok.Getter;


@Getter
public enum EnumUserStatus {
    INVITED,
    OTP_PENDING,
    OTP_VERIFIED,
    ACTIVE,
    BLOCKED,
    JOINED
}
