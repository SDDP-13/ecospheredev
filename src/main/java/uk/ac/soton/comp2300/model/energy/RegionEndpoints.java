package uk.ac.soton.comp2300.model.energy;

import java.util.Map;

// from api "https://agilerates.uk/api/"
public class RegionEndpoints {
    public static final Map<String, String> AGILE_RATES_URL = Map.ofEntries(
        Map.entry("A", "https://agilerates.uk/api/agile_rates_region_A.json"),
        Map.entry("B", "https://agilerates.uk/api/agile_rates_region_B.json"),
        Map.entry("C", "https://agilerates.uk/api/agile_rates_region_C.json"),
        Map.entry("D", "https://agilerates.uk/api/agile_rates_region_D.json"),
        Map.entry("E", "https://agilerates.uk/api/agile_rates_region_E.json"),
        Map.entry("F", "https://agilerates.uk/api/agile_rates_region_F.json"),
        Map.entry("G", "https://agilerates.uk/api/agile_rates_region_G.json"),
        Map.entry("H", "https://agilerates.uk/api/agile_rates_region_H.json"),
        Map.entry("J", "https://agilerates.uk/api/agile_rates_region_J.json"),
        Map.entry("K", "https://agilerates.uk/api/agile_rates_region_K.json"),
        Map.entry("L", "https://agilerates.uk/api/agile_rates_region_L.json"),
        Map.entry("M", "https://agilerates.uk/api/agile_rates_region_M.json"),
        Map.entry("N", "https://agilerates.uk/api/agile_rates_region_N.json"),
        Map.entry("P", "https://agilerates.uk/api/agile_rates_region_P.json")
    );

    public static String urlForRegion(String regionLetter) {
        if (regionLetter == null) return AGILE_RATES_URL.get("A");
        return AGILE_RATES_URL.getOrDefault(regionLetter.toUpperCase(), AGILE_RATES_URL.get("A"));
    }

    private RegionEndpoints() {}
}
