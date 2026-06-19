package com.carland.carland_service.service.impl;

import com.carland.carland_service.feign.NhtsaFeign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VinService {

    private final NhtsaFeign nhtsaFeign;

    public Map<String, Object> decodeVin(String vin) {
        return nhtsaFeign.decodeVin(vin, "json");
    }

    public Map<String, String> extractFieldsFromVin(String vin) {
        Map<String, Object> response = decodeVin(vin);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("Results");

        Map<String, String> output = new HashMap<>();

        for (Map<String, Object> item : results) {
            String variable = (String) item.get("Variable");
            String value = item.get("Value") != null ? item.get("Value").toString() : null;

            switch (variable) {
                case "Make" -> output.put("brand", value);
                case "Model" -> output.put("model", value);
                case "Model Year" -> output.put("modelYear", value);
                case "Body Class" -> output.put("bodyType", value);
                case "Transmission Style" -> output.put("transmissionType", value);
                case "Displacement (L)" -> output.put("engineVolume", value);
                case "Fuel Type - Primary" -> output.put("engineType", value);
            }
        }

        return output;
    }
}
