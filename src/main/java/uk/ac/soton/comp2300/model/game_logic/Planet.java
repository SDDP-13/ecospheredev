package uk.ac.soton.comp2300.model.game_logic;

import uk.ac.soton.comp2300.model.Resource;

import java.util.*;

public class Planet {
    private String name;
    private List<BuildingData> buildingData;
    private Map<Resource, Double> productionMultipliers;
    private static final double MIN_MULT  = 0.7;
    private static final double MAX_MULT = 1.5;

    public Planet(String name) {
        this.name = name;
        this.buildingData = new ArrayList<BuildingData>();
        this.productionMultipliers = new HashMap<Resource, Double>();
        initMultipliers();
    }

    private void initMultipliers() {
        Random random = new Random();

        for (Resource r : Resource.values()) {
            double multiplier = MIN_MULT + (MAX_MULT - MIN_MULT) * random.nextDouble();
            double rounded = Math.round(multiplier * 100.0) / 100.0;
            productionMultipliers.put(r, rounded);
        }
    }

    public String getName() { return name; }
    public List<BuildingData> getBuildingData() { return buildingData; }
    public void addBuilding(BuildingData bData) { buildingData.add(bData); }
    public Map<Resource, Double> getProductionMultipliers() { return productionMultipliers; }
}
