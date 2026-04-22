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

    private int buildingsPlaced = 0; // Tracks lifetime or session building count

    public int getBuildingsPlaced() {
        return buildingsPlaced;
    }

    public void incrementBuildingsPlaced() {
        this.buildingsPlaced++;
    }

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


