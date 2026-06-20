package com.carland.carland_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the token response received from Hyper.
 * JSON response fields are mapped to Java fields using @JsonProperty.
 */
@Data
public class HyperTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;
}