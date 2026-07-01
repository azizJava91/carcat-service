package com.carland.carland_service.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class HmacSignatureValidator {

    public static final String HEADER_NAME = "X-Signature";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;

    public HmacSignatureValidator(@Value("${carland.webhook.signature-secret:}") String secret) {
        this.secret = secret;
    }

    public String sign(byte[] payload) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("Webhook signature secret is not configured");
        }
        return HexFormat.of().formatHex(computeMac(payload));
    }

    public boolean isValid(HttpServletRequest request, byte[] body) {
        if (!StringUtils.hasText(secret)) {
            return false;
        }
        String provided = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(provided)) {
            return false;
        }
        return isValid(resolvePayload(request, body), provided.trim());
    }

    public byte[] resolvePayload(HttpServletRequest request, byte[] body) {
        if (body != null && body.length > 0) {
            return body;
        }
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)) {
            return queryString.getBytes(StandardCharsets.UTF_8);
        }
        return new byte[0];
    }

    private boolean isValid(byte[] payload, String providedSignature) {
        byte[] expected = computeMac(payload);
        byte[] provided;
        try {
            provided = HexFormat.of().parseHex(providedSignature);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return MessageDigest.isEqual(expected, provided);
    }

    private byte[] computeMac(byte[] payload) {
        byte[] data = payload != null ? payload : new byte[0];
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC signature", e);
        }
    }
}
