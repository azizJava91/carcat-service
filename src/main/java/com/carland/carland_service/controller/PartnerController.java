package com.carland.carland_service.controller;

import com.carland.carland_service.dto.request.PartnerRequest;
import com.carland.carland_service.dto.response.PartnerResponse;
import com.carland.carland_service.service.interfaces.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partner")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping("/create")
    public PartnerResponse createPartner(@RequestBody PartnerRequest request,
                                         @RequestHeader("Authorization") String token,
                                         @RequestHeader("phoneNumber") String phoneNumber,
                                         @RequestHeader("Accept-Language") String acceptLanguage) {
        return partnerService.createPartner(request, phoneNumber, acceptLanguage);
    }

    @PostMapping("/edit")
    public PartnerResponse updatePartner(@RequestBody PartnerRequest request,
                                         @RequestHeader("Authorization") String token,
                                         @RequestHeader("phoneNumber") String phoneNumber,
                                         @RequestHeader("Accept-Language") String acceptLanguage) {
        return partnerService.updatePartner(request, phoneNumber, acceptLanguage);
    }
}
