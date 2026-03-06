package uk.ac.soton.comp2300.recommendation_logic;
// Picks the best time slot and returns it to user when queried

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class RecommendationService {

    private CarbonIntensityService carbonService = new CarbonIntensityService();

    public String getRecommendation(String appliance){
        try{
            List<CarbonSlot> slots = carbonService.get24HourForecast();

            CarbonSlot best = slots.stream()
                .min(Comparator.comparingInt(CarbonSlot::getForecast))
                .orElse(null);

            if (best == null){
                return "No recommendations available.";
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            return appliance + ": Best time is "
                + best.getFrom().format(formatter)
                + " - "
                + best.getTo().format(formatter)
                + " (low carbon intensity: "
                + best.getForecast()
                + " gCO2/kWh).";
        } catch (Exception e){
            e.printStackTrace();
            return getFallbackRecommendation(appliance);
        }
    }

    private String getFallbackRecommendation(String appliance){
        switch(appliance.toLowerCase()){
            case "washing machine":
                return "Best time is 22:00-06:00.";
            case "dish washer":
                return "Best time is 23:00-07:00.";
            case "radiator":
                return "Best time is 03:00-07:00.";
            case "air conditioner":
                return "Best time is 11:00-18:00.";
            case "tv":
                return "Best time is 15:00-17:00.";
            case "garden lights":
                return "Best time is 06:00-14:00.";
            default:
                return "";
        }
    }
    
}
