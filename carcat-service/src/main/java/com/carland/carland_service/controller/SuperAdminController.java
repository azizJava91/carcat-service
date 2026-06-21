package com.carland.carland_service.controller;

import com.carland.carland_service.service.interfaces.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {
    private final SuperAdminService superAdminService;


}
