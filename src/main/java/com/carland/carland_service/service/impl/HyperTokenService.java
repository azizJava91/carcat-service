package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.response.HyperTokenResponse;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.feign.HyperAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class HyperTokenService {

    private final HyperAuthClient hyperAuthClient;

    private static final String CACHE_KEY = "hyper_token";

    private final CacheManager cacheManager;

    @Value("${hyper.auth.client-id}")
    private String clientId;

    @Value("${hyper.auth.client-secret}")
    private String clientSecret;

    @Scheduled(fixedRate = 59 * 60 * 1000)
    public void refreshToken() {
        fetchTokenAndCache();
    }

    private String fetchTokenAndCache() {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);

            HyperTokenResponse response = hyperAuthClient.getToken(form);
            if (response == null || response.getAccessToken() == null) {
                throw new MissingFieldException("Hyper service token response is null");
            }
            log.info("HyperTokenResponse {}", response);
            String token = response.getAccessToken();

            Cache cache = cacheManager.getCache("hyper");
            if (cache != null) {
                cache.put(CACHE_KEY, token);
                log.info("token putting into cache");

            }
            log.info("token fetch process is successful");
            return token;

        } catch (Exception e) {
            log.error("Token fetch error", e);
            return null;
        }
    }


    public String getToken() {
        Cache cache = cacheManager.getCache("hyper");

        if (cache != null) {
            String token = cache.get(CACHE_KEY, String.class);
            if (token != null)
                return token;
        }

        return fetchTokenAndCache();
    }
}
