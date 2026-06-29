package com.carland.carland_service.enums;

import java.util.Optional;

/**
 * Maps Hyper's raw {@code universalServiceId} (kept as String, exactly as Hyper sends it)
 * to our canonical {@code ServiceEntity.serviceName}.
 *
 * <p>We never convert Hyper's id type in their payload; we only translate it
 * internally so the sync can locate the matching {@code Percentage} of a car by
 * its serviceName.</p>
 *
 * <p>Hyper currently sends the canonical service name itself as the universalServiceId,
 * so each entry matches against its own serviceName. If Hyper later starts sending
 * additional id forms (e.g. numeric codes), pass them as extra args and matching will
 * cover them too. Any value not listed here (e.g. "other") resolves to empty and is
 * skipped silently by the caller.</p>
 */
public enum HyperServiceMapping {

    AIR_FILTER("Air Filter"),
    BATTERY("Battery"),
    BRAKE_FLUID("Brake Fluid"),
    BRAKE_PADS("Brake Pads"),
    CABIN_FILTER("Cabin Filter"),
    COOLANT("Coolant"),
    ENGINE_OIL("Engine Oil", "Engine oil & filter"),
    FUEL_FILTER("Fuel Filter"),
    GLOW_PLUGS("Glow Plugs"),
    HV_BATTERY_COOLANT("Hv Battery Coolant"),
    INVERTER_COOLANT("Inverter Coolant"),
    LPG_GAS_FILTER("Lpg Gas Filter"),
    LPG_INJECTORS("Lpg Injectors"),
    LPG_REDUCER("Lpg Reducer"),
    POWER_STEERING_FLUID("Power Steering Fluid"),
    REDUCTION_GEAR_OIL("Reduction Gear Oil"),
    SPARK_PLUGS("Spark Plugs"),
    TIMING_BELT("Timing Belt"),
    TIRES("Tires"),
    TRANSMISSION_FLUID("Transmission Fluid"),
    WHEEL_ALIGNMENT("Wheel Alignment"),
    WHEEL_BALANCING("Wheel Balancing");

    /** Our canonical service name (matches ServiceEntity.serviceName). */
    private final String serviceName;

    /** Extra Hyper universalServiceId form(s); the serviceName itself is always matched. */
    private final String[] extraHyperIds;

    HyperServiceMapping(String serviceName, String... extraHyperIds) {
        this.serviceName = serviceName;
        this.extraHyperIds = extraHyperIds;
    }

    public String getServiceName() {
        return serviceName;
    }

    /**
     * Resolve a Hyper universalServiceId (raw String) to our canonical serviceName.
     * Returns empty when there is no mapping (caller must skip silently).
     */
    public static Optional<String> toServiceName(String hyperUniversalServiceId) {
        if (hyperUniversalServiceId == null || hyperUniversalServiceId.isBlank()) {
            return Optional.empty();
        }
        String normalized = hyperUniversalServiceId.trim();
        for (HyperServiceMapping mapping : values()) {
            if (mapping.serviceName.equalsIgnoreCase(normalized)) {
                return Optional.of(mapping.serviceName);
            }
            for (String id : mapping.extraHyperIds) {
                if (id.equalsIgnoreCase(normalized)) {
                    return Optional.of(mapping.serviceName);
                }
            }
        }
        return Optional.empty();
    }
}
