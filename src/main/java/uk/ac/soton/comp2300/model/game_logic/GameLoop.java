package uk.ac.soton.comp2300.model.game_logic;


import uk.ac.soton.comp2300.model.Resource;

public class GameLoop {

    GameState state;
    private long lastProductiontick = System.currentTimeMillis();
    private int baseMoneyAmount = 7;
    private int baseWoodAmount = 5;
    private int baseStoneAmount = 3;
    private int baseMetalAmount = 1;

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


        int resourceAmount;
        Resource resourceGenerated;
        double levelScaling;

        switch (building.getType()) {
            case LUMBER_MILL -> {
                resourceGenerated = Resource.WOOD;
                resourceAmount = baseWoodAmount;
                levelScaling = 1.0;
            }
            case QUARRY -> {
                resourceGenerated = Resource.STONE;
                resourceAmount = baseStoneAmount;
                levelScaling = 1.2;
            }
            case MINE -> {
                resourceGenerated = Resource.METAL;
                resourceAmount = baseMetalAmount;
                levelScaling = 1.4;
            }
            case TOWN -> {
                resourceGenerated = Resource.MONEY;
                resourceAmount = baseMoneyAmount;
                levelScaling = 0.8;
            }
            default -> {return;}
        }

        double planetProdMultiplier = planet.getProductionMultipliers().
                getOrDefault(resourceGenerated, 1.0 );
        double buildingLvl = building.getLevel();
        int amountAdded = (int) Math.round((resourceAmount + (buildingLvl * 2 * levelScaling)) * planetProdMultiplier);
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
