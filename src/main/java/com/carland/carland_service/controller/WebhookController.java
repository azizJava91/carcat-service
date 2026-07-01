package com.carland.carland_service.controller;

import com.carland.carland_service.dto.request.PartnerUpdateServiceVisitRequest;
import com.carland.carland_service.dto.response.v2.CarVinServiceHistoryV2Response;
import com.carland.carland_service.dto.response.v2.PartnerNewServiceVisitResult;
import com.carland.carland_service.dto.response.v2.PartnerUpdateServiceVisitResult;
import com.carland.carland_service.repository.CarRepository;
import com.carland.carland_service.service.PartnerServiceVisitIngestService;
import com.carland.carland_service.service.PartnerServiceVisitUpdateService;
import com.carland.carland_service.util.InternalTokenValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhook/partner")
@RequiredArgsConstructor
public class WebhookController {

    private final InternalTokenValidator internalTokenValidator;
    private final CarRepository carRepository;
    private final PartnerServiceVisitIngestService partnerServiceVisitIngestService;
    private final PartnerServiceVisitUpdateService partnerServiceVisitUpdateService;

    @ModelAttribute
    void requireInternalToken(HttpServletRequest request) {
        if (!internalTokenValidator.isValid(request)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing internal token");
        }
    }

    @GetMapping("/test")
    public String test() {
        return "test basarili oldu";
    }

    @GetMapping("/car/find")
    public ResponseEntity<Void> findCarByVin(@RequestParam String vin) {
        if (vin == null || vin.isBlank() || carRepository.findByVin(vin.trim()) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/new-service-visit")
    public ResponseEntity<PartnerNewServiceVisitResult> newServiceVisit(@RequestBody CarVinServiceHistoryV2Response request) {
        PartnerNewServiceVisitResult result = partnerServiceVisitIngestService.ingest(request);
        return ResponseEntity.status(resolveIngestStatus(result)).body(result);
    }

    @PutMapping("/edit/service-visit")
    public ResponseEntity<PartnerUpdateServiceVisitResult> updateServiceVisit(@RequestBody PartnerUpdateServiceVisitRequest request) {
        PartnerUpdateServiceVisitResult result = partnerServiceVisitUpdateService.update(request);
        return ResponseEntity.status(resolveUpdateStatus(result)).body(result);
    }

    private HttpStatus resolveUpdateStatus(PartnerUpdateServiceVisitResult result) {
        if (result.getVisitFieldsUpdated() > 0 || result.getLinesUpdated() > 0 || result.getPartsUpdated() > 0) {
            return HttpStatus.OK;
        }
        return HttpStatus.CONFLICT;
    }

    private HttpStatus resolveIngestStatus(PartnerNewServiceVisitResult result) {
        if (result.getVisitsCreated() > 0 || result.getLinesCreated() > 0 || result.getPartsCreated() > 0) {
            return HttpStatus.OK;
        }
        return HttpStatus.CONFLICT;
    }
}
