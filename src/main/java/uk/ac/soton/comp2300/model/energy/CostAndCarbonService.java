package uk.ac.soton.comp2300.model.energy;

import java.time.*;

public class CostAndCarbonService {

    private RatesProvider rates;
    private CarbonIntensityClient carbon;
    private ZoneId zone;

    public CostAndCarbonService(RatesProvider rates, CarbonIntensityClient carbon, ZoneId zone) {
        this.rates = rates;
        this.carbon = carbon;
        this.zone = zone;
    }

    public CostAndCarbonResult calculate(Device device, ZonedDateTime start, Duration duration) throws Exception {
        double totalKwh = device.energyUsedKwh(duration);
        long totalMinutes = duration == null ? 0 : duration.toMinutes();
        if (totalMinutes <= 0 || totalKwh <= 0) return new CostAndCarbonResult(0, 0, 0);

        int sliceMinutes = 30;
        int slices = (int) Math.max(1, Math.ceil(totalMinutes / (double) sliceMinutes));
        double kwhPerSlice = totalKwh / slices;

        double costPounds = 0.0;
        double co2Kg = 0.0;

        LocalDate day = start.withZoneSameInstant(zone).toLocalDate();

        for (int i = 0; i < slices; i++) {
            ZonedDateTime t = start.plusMinutes((long) i * sliceMinutes).withZoneSameInstant(zone);

            double pencePerKwh = rates.ratePencePerKwh(t);
            costPounds += (kwhPerSlice * pencePerKwh) / 100.0;

            double kgPerKwh = carbon.intensityKgPerKwhAt(day, t, zone);
            co2Kg += (kwhPerSlice * kgPerKwh);
        }

        return new CostAndCarbonResult(totalKwh, costPounds, co2Kg);
    }
}
