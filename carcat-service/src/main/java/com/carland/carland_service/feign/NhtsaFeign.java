package com.carland.carland_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "nhtsaClient", url = "https://vpic.nhtsa.dot.gov/api/vehicles")

public interface NhtsaFeign {
    @GetMapping("/decodevin/{vin}")
    Map<String, Object> decodeVin(@PathVariable("vin") String vin,
                                  @RequestParam("format") String format);
}
