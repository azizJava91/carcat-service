package com.carland.carland_service.dto.response.hyper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class HyperVehicleByVinMockData {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String MOCK_RESOURCE = "/mock/hyper-vehicle-by-vin-mock.json";

    private HyperVehicleByVinMockData() {
    }

    public static HyperVehicleByVinResponse load(String vin) {
        try (InputStream input = HyperVehicleByVinMockData.class.getResourceAsStream(MOCK_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Mock resource not found: " + MOCK_RESOURCE);
            }
            HyperVehicleByVinResponse response = MAPPER.readValue(input, HyperVehicleByVinResponse.class);
            applyMockUniversalServiceIds(response);
            response.setVin(vin);
            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Hyper mock data", e);
        }
    }

    private static void applyMockUniversalServiceIds(HyperVehicleByVinResponse response) {
        if (response.getServiceHistory() == null) {
            return;
        }

        List<Long> idPool = new ArrayList<>();
        for (long id = 142; id <= 206; id++) {
            idPool.add(id);
        }
        Collections.shuffle(idPool);
        Iterator<Long> ids = idPool.iterator();

        for (HyperServiceHistoryItemResponse visit : response.getServiceHistory()) {
            if (visit.getServices() == null) {
                continue;
            }
            for (HyperServiceLineResponse line : visit.getServices()) {
                if (!isMappableUniversalServiceId(line.getUniversalServiceId())) {
                    line.setUniversalServiceId(null);
                    continue;
                }
                if (ids.hasNext()) {
                    line.setUniversalServiceId(String.valueOf(ids.next()));
                } else {
                    line.setUniversalServiceId(null);
                }
            }
        }
    }

    private static boolean isMappableUniversalServiceId(String universalServiceId) {
        return universalServiceId != null
                && !universalServiceId.isBlank()
                && !"other".equalsIgnoreCase(universalServiceId);
    }
}
