package uk.ac.soton.comp2300.model;

import java.nio.file.Path;
import java.time.*;

import uk.ac.soton.comp2300.model.energy.*;

/**
 * Service to calculate potential eco and cost savings for scheduled appliance use.
 * 
 * Example usage:
 * ScheduleTask task = new ScheduleTask();
 * task.setDeviceName("Washing Machine");
 * task.setTime(LocalTime.of(14, 0));
 * task.setDuration(Duration.ofHours(2));
 *
 * EnergyLabel label = EnergyLabel.A;
 *
 * EcoSavingsService service = new EcoSavingsService();
 * try {
 *     EcoSavingsReport report = service.calculate(task, label);
 *     System.out.println("Money saved: £" + report.getMoneySaved());
 *     System.out.println("CO2 saved: " + report.getCo2Saved() + " kg");
 * } catch (Exception e) {
 *     e.printStackTrace();
 * }
 */

public class EcoSavingsService {

    private static final ZoneId ZONE = ZoneId.of("Europe/London");
    private static final String DEFAULT_REGION = "A";
    private static final Path ASSETS_DIR = Path.of("assets");

    public EcoSavingsReport calculate(ScheduleTask task, EnergyLabel label) throws Exception {

        if (task == null) throw new IllegalArgumentException("task is null");
        if (task.getTime() == null) throw new IllegalArgumentException("task.time is null");
        if (task.getDuration() == null) throw new IllegalArgumentException("task.duration is null");

        LocalDate today = LocalDate.now(ZONE);

        // load rates (auto cache daily)
        RatesCacheManager ratesCache = new RatesCacheManager(ASSETS_DIR);
        RatesCacheResult ratesResult = ratesCache.updateDaily(DEFAULT_REGION, today);
        RatesProvider rates = ratesCache.loadProviderFromCached(ratesResult.getPath());

        // load carbon (auto cache daily)
        CarbonIntensityClient carbon = new CarbonIntensityClient(ASSETS_DIR);
        carbon.updateDaily(today);

        // peak window cache, only compute when JSON updated OR cache missing
        PeakWindowCache peakCache = new PeakWindowCache(ASSETS_DIR);
        PeakWindow peakWindow;

        boolean peakMissing = !peakCache.exists(DEFAULT_REGION, today);
        if (ratesResult.isDownloaded() || peakMissing) {
            peakWindow = rates.inferPeakWindowForDate(today, ZONE);
            peakCache.write(DEFAULT_REGION, today, peakWindow);
        } else {
            peakWindow = peakCache.read(DEFAULT_REGION, today);
        }

        // build device, will be replace soon once we change the ScheduleScene.java to pass in device type directly
        ApplianceType type = DeviceTypeMapper.fromDeviceName(task.getDeviceName());
        Device device = new Device(type, label);

        ZonedDateTime scheduledStart = ZonedDateTime.of(today, task.getTime(), ZONE);

        // peak baseline start = peak window start (same day); if already peak, baseline=scheduled
        ZonedDateTime peakStart = ZonedDateTime.of(today, peakWindow.getStartInclusive(), ZONE);
        if (peakWindow.contains(scheduledStart.toLocalTime())) {
            peakStart = scheduledStart;
        }

        // calculate current vs peak
        CostAndCarbonService calc = new CostAndCarbonService(rates, carbon, ZONE);

        CostAndCarbonResult current = calc.calculate(device, scheduledStart, task.getDuration());
        CostAndCarbonResult peak = calc.calculate(device, peakStart, task.getDuration());

        double moneySaved = peak.getCostPounds() - current.getCostPounds();
        double co2Saved = peak.getCo2Kg() - current.getCo2Kg();

        return new EcoSavingsReport(current, peak, moneySaved, co2Saved, peakWindow);
    }
}
