package uk.ac.soton.comp2300.model.game_logic;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.ResourceStack;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<Planet>  planets;
    private String selectedPlanetId;

    private ResourceStack moneyTotal;
    private ResourceStack woodTotal;
    private ResourceStack metalTotal;
    private ResourceStack stoneTotal;
    private int totalXp;

    private int researchLevel;


    private double totalEnergySaved = 0.0;
    private double totalCo2Saved = 0.0;
    private double totalMoneySaved = 0.0;
    private java.util.Map<String, Double> dailySavingsMap = new java.util.HashMap<>();
    private java.util.Map<String, Double> dailyTaskCompletionMap = new java.util.HashMap<>();

    public double getTotalEnergySaved() { return totalEnergySaved; }
    public void setTotalEnergySaved(double val) { this.totalEnergySaved = val; }

    public double getTotalCo2Saved() { return totalCo2Saved; }
    public void setTotalCo2Saved(double val) { this.totalCo2Saved = val; }

    public double getTotalMoneySaved() { return totalMoneySaved; }
    public void setTotalMoneySaved(double val) { this.totalMoneySaved = val; }

    public java.util.Map<String, Double> getDailySavingsMap() { return dailySavingsMap; }
    public java.util.Map<String, Double> getDailyTaskCompletionMap() { return dailyTaskCompletionMap; }

    private int buildingsPlaced = 0; // Tracks lifetime or session building count

    public int getBuildingsPlaced() {
        return buildingsPlaced;
    }

    public void incrementBuildingsPlaced() {
        this.buildingsPlaced++;
    }
    public void decrementBuildingsPlaced() {  this.buildingsPlaced--; }

    public GameState() {
        this.planets = new ArrayList<Planet>();
        this.moneyTotal = new ResourceStack(Resource.MONEY, 0);
        this.woodTotal = new ResourceStack(Resource.WOOD, 0);
        this.metalTotal = new ResourceStack(Resource.METAL, 0);
        this.stoneTotal = new ResourceStack(Resource.STONE, 0);
    }
    public int getTotalXp() { return totalXp; }

    public void addXp(int amount) {
        this.totalXp += amount;
    }

    public void addResource(Resource type, int amount) {
        switch (type) {
            case MONEY -> moneyTotal.add(amount);
            case WOOD -> woodTotal.add(amount);
            case METAL -> metalTotal.add(amount);
            case STONE -> stoneTotal.add(amount);
        }
    }

    public void spendResources(List<ResourceStack> cost) {
        if (!sufficientResources(cost)) {
            throw new IllegalStateException("Not enough resources to spend cost");
        }

        for (ResourceStack stack : cost) {
            Resource type = stack.getType();
            int amount = stack.getAmount();
            switch (type) {
                case MONEY -> moneyTotal.add(-amount);
                case WOOD -> woodTotal.add(-amount);
                case METAL -> metalTotal.add(-amount);
                case STONE -> stoneTotal.add(-amount);
            }
        }
    }

    public int getResourceAmount(Resource type) {
        return switch (type) {
            case MONEY -> moneyTotal.getAmount();
            case WOOD -> woodTotal.getAmount();
            case METAL -> metalTotal.getAmount();
            case STONE -> stoneTotal.getAmount();
        };
    }

    public void addPlanet(Planet planet) { planets.add(planet); }
    public List<Planet> getPlanets() { return planets; }

    public void setSelectedPlanet(Planet planet) { this.selectedPlanetId = (planet == null) ? null : planet.getId(); }
    public Planet getSelectedPlanet() {
        if (selectedPlanetId == null) return null;

        for (Planet planet : planets) {
            if (selectedPlanetId.equals(planet.getId())) return planet;
        }
        return null;
    }


    /**--------Checks if player has sufficient resources to build item*/
    public boolean sufficientResources (List<ResourceStack> costs) {
        for (ResourceStack cost : costs) {
            if (getResourceAmount(cost.getType()) < cost.getAmount()) return false;
        }
        return true;
    }

}