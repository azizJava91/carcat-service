package com.carland.carland_service.controller;

import com.carland.carland_service.repository.CarRepository;
import com.carland.carland_service.util.InternalTokenValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
}
