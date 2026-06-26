package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.PartnerRequest;
import com.carland.carland_service.dto.response.PartnerResponse;

public interface PartnerService {

    PartnerResponse createPartner(PartnerRequest request, String phoneNumber, String acceptLanguage);

    PartnerResponse updatePartner(PartnerRequest request, String phoneNumber, String acceptLanguage);
}
