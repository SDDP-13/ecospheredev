package uk.ac.soton.comp2300.model.game_logic;


import uk.ac.soton.comp2300.model.Resource;

public class GameLoop {

    GameState state;
    private long lastProductiontick = System.currentTimeMillis();
    private int baseResourceamount = 1;

    public GameLoop(GameState state) {
        this.state = state;
    }

    /**Applies Resources produced by buildings on each planet to the Users resource stash**/

    private void produceResourcesForBuildings(){

        for (Planet planet : state.getPlanets()){

            for (BuildingData building: planet.getBuildingData()) {
                produceBuildingRes(planet, building);
            }
        }
    }

    /**Takes a planet and a building and calculates the resources to be applied to the user's resource stash**/
    private void produceBuildingRes( Planet planet, BuildingData building) {


        int resourceAmount = baseResourceamount;
        Resource resourceGenerated = null;

        switch (building.getType()) {
            case LUMBER_MILL -> {
                resourceGenerated = Resource.WOOD;
            }
            case QUARRY -> {
                resourceGenerated = Resource.STONE;
            }
            case MINE -> {
                resourceGenerated = Resource.METAL;
            }
            case TOWN -> {
                resourceGenerated = Resource.MONEY;
            }
            default -> {return;}
        }

        double planetProdMultiplier = planet.getProductionMultipliers().
                getOrDefault(resourceGenerated, 1.0 );
        double buildingLvl = building.getLevel();
        int amountAdded = (int) Math.round(resourceAmount*(planetProdMultiplier + buildingLvl));
        state.addResource(resourceGenerated, amountAdded );

    }


    public void tick() {
        long thisTick = System.currentTimeMillis();

        if (thisTick - lastProductiontick >= 3000) {
            produceResourcesForBuildings();
            lastProductiontick = thisTick;

        }
    }


}
