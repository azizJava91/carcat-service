package com.carland.carland_service.enums;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.Getter;

@Getter
public enum EnumRangeStatus {
    AVAILABLE,
    ACCEPTED,
    REJECTED,
    PENDING,
    BREAK,
    PENDING_LOCAL,
    FULL
}
