package uk.ac.soton.comp2300.model.energy;

import java.util.EnumMap;
import java.util.Map;

public class DeviceWattsEstimator {

    private static final Map<ApplianceType, Double> BASE_WATTS = new EnumMap<>(ApplianceType.class);
    static {
        BASE_WATTS.put(ApplianceType.WASHING_MACHINE, 2000.0);
        BASE_WATTS.put(ApplianceType.DISHWASHER,      1800.0);
        BASE_WATTS.put(ApplianceType.DRYER,           2500.0);
        BASE_WATTS.put(ApplianceType.RADIATOR,        1500.0);
        BASE_WATTS.put(ApplianceType.AIR_CONDITIONER, 1200.0);
        BASE_WATTS.put(ApplianceType.TV,               120.0);
        BASE_WATTS.put(ApplianceType.GARDEN_LIGHTS,     60.0);
        BASE_WATTS.put(ApplianceType.OTHER,            500.0);
    }

    public static double labelMultiplier(EnergyLabel label) {
        if (label == null) return 1.0;
        return switch (label) {
            case A -> 0.75;
            case B -> 0.85;
            case C -> 0.95;
            case D -> 1.05;
            case E -> 1.15;
            case F -> 1.25;
            case G -> 1.35;
        };
    }

    public static double estimateWatts(ApplianceType type, EnergyLabel label) {
        if (type == null) type = ApplianceType.OTHER;
        double base = BASE_WATTS.getOrDefault(type, BASE_WATTS.get(ApplianceType.OTHER));
        return base * labelMultiplier(label);
    }

    private DeviceWattsEstimator() {}
}