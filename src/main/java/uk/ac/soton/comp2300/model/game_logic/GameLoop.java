package uk.ac.soton.comp2300.model.game_logic;


import uk.ac.soton.comp2300.model.Resource;

public class GameLoop {

    GameState state;
    private long lastProductiontick = System.currentTimeMillis();

    public GameLoop(GameState state) {
        this.state = state;

    }

    private void produceResourcesForBuildings(){
        System.out.println("Planet count: " + state.getPlanets().size());
        for (Planet planet : state.getPlanets()){
            System.out.println("Planet: " + planet.getName());
            System.out.println("Building count: " + planet.getBuildingData().size());
            for (BuildingData building: planet.getBuildingData()) {
                System.out.println("Found building: " + building.getType());
                produceBuildingRes(building.getType());
            }
        }
    }

    private void produceBuildingRes(BuildingType buildingType) {
        System.out.println("checkingBuildings");
        switch (buildingType) {
            case LUMBER_MILL -> {
                state.addResource(Resource.WOOD, 1);
                System.out.println("+ 1 Wood");
            }
            case QUARRY -> {
                state.addResource(Resource.STONE, 1);
                System.out.println("+1 Stone");
            }
            case MINE -> {
                state.addResource(Resource.METAL, 1);
                System.out.println("+1 Metal");
            }
            case TOWN -> {
                state.addResource(Resource.MONEY, 1);
                System.out.println("+1 Money");
            }
            default -> {}


        }
    }

    public void tick() {
        long thisTick = System.currentTimeMillis();

        if (thisTick - lastProductiontick >= 3000) {
            produceResourcesForBuildings();
            lastProductiontick = thisTick;
            System.out.println("Resources produced");
        }
    }


}
