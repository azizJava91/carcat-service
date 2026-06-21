package com.carland.carland_service.enums;

import feign.Body;
import lombok.Getter;

@Getter

public enum EnumUserRoles {
    BOSS,
    SUPER_ADMIN,
    ADMIN,
    USER;
}
