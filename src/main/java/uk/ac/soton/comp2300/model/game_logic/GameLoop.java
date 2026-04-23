package uk.ac.soton.comp2300.model.game_logic;


import uk.ac.soton.comp2300.model.Resource;

public class GameLoop {

    GameState state;
    private long lastProductiontick = System.currentTimeMillis();

    public GameLoop(GameState state) {
        this.state = state;

    }

    private void produceResourcesForBuildings(){

        for (Planet planet : state.getPlanets()){

            for (BuildingData building: planet.getBuildingData()) {
                int buildingLevel = building.getLevel();

                produceBuildingRes(building.getType(), building.getLevel());
            }
        }
    }

    private void produceBuildingRes(BuildingType buildingType, int buildingLvl) {
        switch (buildingType) {
            case LUMBER_MILL -> {
                state.addResource(Resource.WOOD, (1*buildingLvl));

            }
            case QUARRY -> {
                state.addResource(Resource.STONE, (1*buildingLvl));

            }
            case MINE -> {
                state.addResource(Resource.METAL, (1*buildingLvl));

            }
            case TOWN -> {
                state.addResource(Resource.MONEY, (1*buildingLvl));

            }
            default -> {}


        }
    }

    public void tick() {
        long thisTick = System.currentTimeMillis();

        if (thisTick - lastProductiontick >= 3000) {
            produceResourcesForBuildings();
            lastProductiontick = thisTick;

        }
    }


}
