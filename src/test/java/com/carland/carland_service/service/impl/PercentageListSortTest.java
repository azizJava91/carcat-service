package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.response.CarServicePercentageResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies GET /service/percentages sort order: lowest remaining % (most urgent) first.
 * Score logic mirrors {@link CarServiceImpl#remainingServiceScore(CarServicePercentageResponse)}.
 */
class PercentageListSortTest {

    private static int remainingServiceScore(CarServicePercentageResponse item) {
        Integer kmRemaining = item.getKmPercentage();
        Integer monthRemaining = item.getMonthPercentageDigit() != null
                ? item.getMonthPercentageDigit()
                : item.getMonthPercentage();

        if (kmRemaining == null && monthRemaining == null) {
            return Integer.MAX_VALUE;
        }
        if (kmRemaining == null) {
            return monthRemaining;
        }
        if (monthRemaining == null) {
            return kmRemaining;
        }
        return Math.min(kmRemaining, monthRemaining);
    }

    @Test
    void sortsMostUrgentServiceFirst() {
        CarServicePercentageResponse urgent = CarServicePercentageResponse.builder()
                .serviceName("Engine Oil")
                .kmPercentage(15)
                .monthPercentageDigit(90)
                .build();
        CarServicePercentageResponse relaxed = CarServicePercentageResponse.builder()
                .serviceName("Air Filter")
                .kmPercentage(80)
                .monthPercentageDigit(70)
                .build();

        List<CarServicePercentageResponse> list = new ArrayList<>(List.of(relaxed, urgent));
        list.sort(Comparator
                .comparingInt(PercentageListSortTest::remainingServiceScore)
                .thenComparing(CarServicePercentageResponse::getServiceName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        assertEquals("Engine Oil", list.get(0).getServiceName());
        assertEquals("Air Filter", list.get(1).getServiceName());
    }

    @Test
    void usesMinOfKmAndMonthWhenBothPresent() {
        CarServicePercentageResponse timeUrgent = CarServicePercentageResponse.builder()
                .serviceName("Battery")
                .kmPercentage(90)
                .monthPercentageDigit(5)
                .build();
        CarServicePercentageResponse kmUrgent = CarServicePercentageResponse.builder()
                .serviceName("Tires")
                .kmPercentage(10)
                .monthPercentageDigit(95)
                .build();

        List<CarServicePercentageResponse> list = new ArrayList<>(List.of(timeUrgent, kmUrgent));
        list.sort(Comparator.comparingInt(PercentageListSortTest::remainingServiceScore));

        assertEquals("Battery", list.get(0).getServiceName());
        assertEquals("Tires", list.get(1).getServiceName());
    }
}
