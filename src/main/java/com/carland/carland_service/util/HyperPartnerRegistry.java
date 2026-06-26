package com.carland.carland_service.util;

public final class HyperPartnerRegistry {

    public static final long HYPERSERVICE_CENTER_ID = 1L;
    public static final String HYPERSERVICE_CENTER_NAME = "HyperService";

    private HyperPartnerRegistry() {
    }

    public static Long resolveServiceCenterId(Long upstreamId) {
        return upstreamId != null ? upstreamId : HYPERSERVICE_CENTER_ID;
    }

    public static String resolveServiceCenterName(Long serviceCenterId) {
        if (serviceCenterId == null || serviceCenterId == HYPERSERVICE_CENTER_ID) {
            return HYPERSERVICE_CENTER_NAME;
        }
        return HYPERSERVICE_CENTER_NAME;
    }
}
