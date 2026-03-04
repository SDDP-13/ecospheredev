package uk.ac.soton.comp2300.model.energy;

// when adding new appliance types, also update:
// DeviceWattsEstimator.BASE_WATTS and DeviceTypeMapper (if needed)

public enum ApplianceType {
    WASHING_MACHINE,
    DISHWASHER,
    DRYER,
    RADIATOR,
    AIR_CONDITIONER,
    TV,
    GARDEN_LIGHTS,
    OTHER
}