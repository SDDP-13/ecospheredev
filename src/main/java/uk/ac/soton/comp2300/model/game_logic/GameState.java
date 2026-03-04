package uk.ac.soton.comp2300.model.game_logic;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.ResourceStack;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<Planet>  planets;
    private Planet selectedPlanet;

    private ResourceStack moneyTotal;
    private ResourceStack woodTotal;
    private ResourceStack metalTotal;
    private ResourceStack stoneTotal;

    public GameState() {
        this.planets = new ArrayList<Planet>();
        this.moneyTotal = new ResourceStack(Resource.MONEY, 0);
        this.woodTotal = new ResourceStack(Resource.WOOD, 0);
        this.metalTotal = new ResourceStack(Resource.METAL, 0);
        this.stoneTotal = new ResourceStack(Resource.STONE, 0);
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
    public void setSelectedPlanet(Planet selectedPlanet) { this.selectedPlanet = selectedPlanet; }
    public Planet getSelectedPlanet() { return selectedPlanet; }
}

