package com.carland.carland_service.feign;

import com.carland.carland_service.dto.response.NameSurname;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "nameSurname", url = "https://digital-innovation.agency/auth/server/api/v1/users")

public interface NameSurnameFeign {
    @GetMapping("/getNameSurname")
    NameSurname getNameSurname(@RequestParam Long userId);
}
