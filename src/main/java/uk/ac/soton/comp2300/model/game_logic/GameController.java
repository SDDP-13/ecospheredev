package uk.ac.soton.comp2300.model.game_logic;

import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.ResourceStack;

import java.util.List;

public class GameController {

    private GameState state;
    private GameLoop gameLoop;

    private static final double MIN_ANGULAR_DISTANCE = Math.toRadians(5);


    public GameController(GameState loadedState) {
        this.state = loadedState;
        this.gameLoop = new GameLoop(state);
    }

    public void initializeNewGame() {
        Planet earth = new Planet("Earth");
        state.addPlanet(earth);
        state.setSelectedPlanet(earth);


        state.addResource(Resource.MONEY, 15000);
        state.addResource(Resource.METAL, 4000);
        state.addResource(Resource.WOOD, 8000);
        state.addResource(Resource.STONE, 4000);
    }

    public void gameLoopTick(){
        gameLoop.tick();
    }

    public boolean isBuildLocationFree(Planet planet, double theta, double phi) {

        for (BuildingData existing : planet.getBuildingData()) {

            double dTheta = existing.getTheta() - theta;
            double dPhi = existing.getPhi() - phi;

            double distance = Math.sqrt(dTheta * dTheta + dPhi * dPhi);

            if (distance < MIN_ANGULAR_DISTANCE) {
                return false;
            }
        }

        return true;
    }

    public boolean canPlaceBuilding(BuildingType type, Planet planet, double theta, double phi) {
        if (planet == null) return false;
        if (!isBuildLocationFree(planet, theta, phi)) return false;
        return state.sufficientResources(type.getPrice());
    }

    public BuildingData placeBuilding(
            BuildingType type,
            double theta,
            double phi
    ) {
        Planet planet = getSelectedPlanet();
        if (!canPlaceBuilding(type, planet, theta, phi)) { return null; }

        List<ResourceStack> cost = type.getPrice();
        state.spendResources(cost);

        BuildingData building = new BuildingData(type, theta, phi);
        planet.addBuilding(building);
        state.incrementBuildingsPlaced();
        return building;
    }

    public BuildingData placeBuildingTest(
            Planet planet,
            BuildingType type,
            double theta,
            double phi
    ) {
        if (planet == null) {
            return null;
        }

        if (!isBuildLocationFree(planet, theta, phi)) {
            return null;
        }

        BuildingData building = new BuildingData(type, theta, phi);
        planet.addBuilding(building);

        return building;
    }

    public void removeBuilding(Planet planet, BuildingData building) {
        if (planet == null || building == null) return;

        boolean removed = planet.removeBuilding(building);

        if (removed) {
            state.decrementBuildingsPlaced();
        }
    }

    public boolean canUpgradeBuilding(BuildingData data) {
        if (data == null) return false;
        if (data.getLevel() >= 5) return false;

        BuildingType type = data.getType();
        if (!type.isUpgradeable()) return false;

        if (isBlockedByResearch(data)) return false;

        List<ResourceStack> cost = type.getUpgradeCost(data.getLevel());
        return state.sufficientResources(cost);
    }

    public boolean upgradeBuilding(BuildingData data) {
        if (!canUpgradeBuilding(data)) return false;

        List<ResourceStack> cost = data.getType().getUpgradeCost(data.getLevel());
        state.spendResources(cost);

        data.levelUp();

        return true;
    }

    public boolean isBlockedByResearch(BuildingData data) {
        return data.getLevel() >= getSelectedPlanet().getResearchLevel();
    }

    public List<ResourceStack> getLaunchCost() {
        int planetsOwned = getPlanets().size();

        int base = 5000;
        int scale = 1000;

        int total = base + ((planetsOwned - 1) * scale);

        return List.of(
                new ResourceStack(Resource.MONEY, total),
                new ResourceStack(Resource.METAL, total/2)
        );
    }

    public boolean canLaunch() { return state.sufficientResources(getLaunchCost()); }

    public boolean launch() {
        if (!canLaunch()) return false;
        List<ResourceStack> cost = getLaunchCost();
        state.spendResources(cost);
        return true;
    }

    public List<ResourceStack> getResearchCost() {
        int count = getNumberOfBuildingType(getSelectedPlanet(), BuildingType.RESEARCH_LAB);
        int base = 1000;
        int scale = 5000;
        int total = base;
        return List.of(
                new ResourceStack(Resource.MONEY, total),
                new ResourceStack(Resource.STONE, total/2),
                new ResourceStack(Resource.WOOD, total/2)
        );
    }
    public boolean canResearchLevel() {
        int count = getNumberOfBuildingType(getSelectedPlanet(), BuildingType.RESEARCH_LAB);
        int currentLevel = getSelectedPlanet().getResearchLevel();
        if (count < currentLevel) return false;
        return state.sufficientResources(getResearchCost()) && currentLevel < 5;
    }

    public boolean increaseResearchLevel() {
        if (!canResearchLevel()) return false;
        List<ResourceStack> cost = getResearchCost();
        state.spendResources(cost);
        getSelectedPlanet().unlockNextResearchLevel();
        return true;
    }

    public int getNumberOfBuildingType(Planet planet, BuildingType type) {
        long labs = planet.getBuildingData().stream()
                .filter(b -> b.getType() == type)
                .count();
        int count = Math.max(1, (int) labs);
        return count;
    }

    public void addPlanet(Planet planet) { getPlanets().add(planet); }
    public void addResource(Resource type, int amount) { state.addResource(type, amount); }
    public int getResourceAmount(Resource type) { return state.getResourceAmount(type); }
    public Planet getSelectedPlanet() { return state.getSelectedPlanet(); }
    public void setSelectedPlanet(Planet planet) { state.setSelectedPlanet(planet); }
    public List<Planet> getPlanets() { return state.getPlanets(); }

    public GameState getGameState() { return state; }

    /**---------Returns whether a building is allowed to be built or not.------------*/
    public boolean buildable(BuildingType type) {
        return state.sufficientResources(type.getPrice());
    }



}
