package uk.ac.soton.comp2300.recommendation_logic;
// Picks the best time slot and returns it to user when queried

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class RecommendationService {

    private CarbonIntensityService carbonService = new CarbonIntensityService();

    // Most recently fetched forecast slots from the API
    private static List<CarbonSlot> cachedForecastSlots = null;
    // When it was fetched
    private static LocalDateTime cacheTimestamp = null;
    private static final int CACHE_EXPIRY_HOURS = 2;

    public String getRecommendation(String appliance) {

        List<CarbonSlot> forecastSlots = null;
        String outputDataSource; // Where the live ouput came from

        try {
            forecastSlots = carbonService.get24HourForecast();
            // Updates the cache to current forecast
            cachedForecastSlots = forecastSlots;
            cacheTimestamp = LocalDateTime.now();
            outputDataSource = "live from API";
        } catch (Exception liveException) {
            System.err.println("Live carbon API failed: " + liveException.getMessage());

            if (cachedForecastSlots != null && cacheTimestamp != null) {
                long hoursSinceCache = java.time.Duration.between(cacheTimestamp, LocalDateTime.now()).toHours();

                if (hoursSinceCache < CACHE_EXPIRY_HOURS) {
                    forecastSlots = cachedForecastSlots;
                    outputDataSource = "cached (" + hoursSinceCache + "hours old)";
                } else {
                    // Cache exists but too old
                    forecastSlots = cachedForecastSlots;
                    outputDataSource = "expired cache";
                }
            } else {
                outputDataSource = "no cache available";
            }
        }

        if (forecastSlots == null) {
            return appliance + ": Recommendation unavailable - could not reach the carbon intensity service & no recent forecast cached. Please check your connection and try again.";
        }

        CarbonSlot bestSlot = forecastSlots.stream().min(Comparator.comparingInt(CarbonSlot::getForecast)).orElse(null);

        if (bestSlot == null){
            return appliance + ": Forecast data was empty. Please try again later.";
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String recommendation = appliance + ": Best time is "
            + bestSlot.getFrom().format(timeFormatter)
            + " - "
            + bestSlot.getTo().format(timeFormatter)
            + " (carbon intensity: " + bestSlot.getForecast() + " gCO2/kWh)";
                
        if (outputDataSource.startsWith("cached")) {
            recommendation += " Based on " + outputDataSource + " data - live feed unavailable.";
        }

        return recommendation;

    }
}