package com.carland.carland_service.dto.response.hyper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

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
            response.setVin(vin);
            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Hyper mock data", e);
        }
    }
}
