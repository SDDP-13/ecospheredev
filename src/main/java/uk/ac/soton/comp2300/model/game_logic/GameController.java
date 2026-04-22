package uk.ac.soton.comp2300.model.game_logic;

import uk.ac.soton.comp2300.model.Resource;

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


        state.addResource(Resource.MONEY, 1000);
        state.addResource(Resource.METAL, 200);
        state.addResource(Resource.WOOD, 500);
        state.addResource(Resource.STONE, 200);
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

    public BuildingData placeBuidling(
            Planet planet,
            BuildingType type,
            double theta,
            double phi
    ) {

        if (!isBuildLocationFree(planet, theta, phi)) {
            return null;
        }


        BuildingData building = new BuildingData(type, theta, phi);
        planet.addBuilding(building);

        state.incrementBuildingsPlaced();
        return building;
    }

    public void addResource(Resource type, int amount) { state.addResource(type, amount); }


    public GameState getGameState() { return state; }

    /**---------Returns whether a building is allowed to be built or not.------------*/
    public boolean buildable(BuildingType type) {
        return state.sufficientResources(type.getPrice());
    }


}
