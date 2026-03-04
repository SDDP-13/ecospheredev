package uk.ac.soton.comp2300.model.energy;

public class DeviceTypeMapper {

    // a tiny patch will be remove soon once we change the ScheduleScene.java
    public static ApplianceType fromDeviceName(String deviceName) {
        if (deviceName == null) return ApplianceType.OTHER;
        String s = deviceName.trim().toLowerCase();

        if (s.contains("wash")) return ApplianceType.WASHING_MACHINE;
        if (s.contains("dish")) return ApplianceType.DISHWASHER;
        if (s.contains("dryer")) return ApplianceType.DRYER;
        if (s.contains("radiator") || s.contains("heater")) return ApplianceType.RADIATOR;
        if (s.contains("air") && s.contains("condition")) return ApplianceType.AIR_CONDITIONER;
        if (s.equals("tv") || s.contains("television")) return ApplianceType.TV;
        if (s.contains("garden") || s.contains("light")) return ApplianceType.GARDEN_LIGHTS;

        return ApplianceType.OTHER;
    }

    private DeviceTypeMapper() {}
}
