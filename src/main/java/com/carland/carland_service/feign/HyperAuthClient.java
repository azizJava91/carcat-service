package com.carland.carland_service.feign;

import com.carland.carland_service.dto.response.HyperTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hyperAuthClient", url = "${hyper.auth.base-url}")
public interface HyperAuthClient {

    @PostMapping(value = "${hyper.auth.token-url}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    HyperTokenResponse getToken(@RequestBody MultiValueMap<String, String> form);

}
