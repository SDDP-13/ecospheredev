package uk.ac.soton.comp2300.model.energy;

import java.time.Duration;

public class Device {
    private ApplianceType type;
    private EnergyLabel label;

    public Device() {}

    public Device(ApplianceType type, EnergyLabel label) {
        this.type = type;
        this.label = label;
    }

    public ApplianceType getType() { return type; }
    public void setType(ApplianceType type) { this.type = type; }

    public EnergyLabel getLabel() { return label; }
    public void setLabel(EnergyLabel label) { this.label = label; }

    public double estimatedWatts() {
        return DeviceWattsEstimator.estimateWatts(type, label);
    }

    public double energyUsedKwh(Duration duration) {
        if (duration == null) return 0.0;
        double hours = duration.toMinutes() / 60.0;
        return (estimatedWatts() / 1000.0) * hours;
    }
}
