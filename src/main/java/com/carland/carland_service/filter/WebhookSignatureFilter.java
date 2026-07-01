package com.carland.carland_service.filter;

import com.carland.carland_service.util.HmacSignatureValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class WebhookSignatureFilter extends OncePerRequestFilter {

    private static final String PARTNER_PREFIX = "/webhook/partner/";
    private static final String TEST_PATH = "/webhook/partner/test";

    private final HmacSignatureValidator hmacSignatureValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith(PARTNER_PREFIX) || TEST_PATH.equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);
        byte[] body = wrapped.getCachedBody();

        if (!hmacSignatureValidator.isValid(wrapped, body)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or missing signature\"}");
            return;
        }

        filterChain.doFilter(wrapped, response);
    }
}
