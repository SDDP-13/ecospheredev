package uk.ac.soton.comp2300.model.game_logic;

import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.ResourceStack;

import java.util.List;

public enum BuildingType {
    LUMBER_MILL(List.of(
            new ResourceStack(Resource.MONEY, 100),
            new ResourceStack(Resource.WOOD, 70),
            new ResourceStack(Resource.METAL, 10),
            new ResourceStack(Resource.STONE, 30)
    )),
    QUARRY(List.of(
            new ResourceStack(Resource.MONEY, 400),
            new ResourceStack(Resource.WOOD, 250),
            new ResourceStack(Resource.METAL, 50),
            new ResourceStack(Resource.STONE, 150)
    )),
    MINE(List.of(
            new ResourceStack(Resource.MONEY, 1000),
            new ResourceStack(Resource.WOOD, 950),
            new ResourceStack(Resource.METAL, 150),
            new ResourceStack(Resource.STONE, 400)
    )),
    TOWN(List.of(
            new ResourceStack(Resource.MONEY, 250),
            new ResourceStack(Resource.WOOD, 250),
            new ResourceStack(Resource.METAL, 20),
            new ResourceStack(Resource.STONE, 80)
    )),
    MARKET(List.of(
            new ResourceStack(Resource.MONEY, 1200),
            new ResourceStack(Resource.WOOD, 1000),
            new ResourceStack(Resource.METAL, 200),
            new ResourceStack(Resource.STONE, 400)
    )),
    SPACEPORT(List.of(
            new ResourceStack(Resource.MONEY, 4000),
            new ResourceStack(Resource.WOOD, 2000),
            new ResourceStack(Resource.METAL, 1200),
            new ResourceStack(Resource.STONE, 1000)
    )),
    RESEARCH_LAB(List.of(
            new ResourceStack(Resource.MONEY, 2500),
            new ResourceStack(Resource.WOOD, 1500),
            new ResourceStack(Resource.METAL, 600),
            new ResourceStack(Resource.STONE, 1000)


    ));

    private final List<ResourceStack> price;

    BuildingType(List<ResourceStack> price) {
        this.price = price;
    }
    public List<ResourceStack> getBasePrice()
    {
        return price;
    }

    public boolean isUpgradeable() {
        return switch (this) {
            case TOWN, MINE, QUARRY, LUMBER_MILL -> true;
            default -> false;
        };
    }

    public List<ResourceStack> getUpgradeCost(int level) {

        double scalingFactor = switch (this) {
            case LUMBER_MILL -> 1.30;
            case QUARRY -> 1.35;
            case MINE -> 1.45;
            case TOWN -> 1.25;
            default -> 1.35;
        };

        int scale = (int) Math.pow(scalingFactor, level);

        return switch (this) {
            case LUMBER_MILL -> List.of(
                    new ResourceStack(Resource.MONEY, 120 * scale),
                    new ResourceStack(Resource.WOOD, 60 * scale),
                    new ResourceStack(Resource.METAL, 10 * scale),
                    new ResourceStack(Resource.STONE, 30 * scale)
            );
            case QUARRY -> List.of(
                    new ResourceStack(Resource.MONEY, 350 * scale),
                    new ResourceStack(Resource.WOOD, 200 * scale),
                    new ResourceStack(Resource.METAL, 40 * scale),
                    new ResourceStack(Resource.STONE, 120 * scale)
            );
            case MINE -> List.of(
                    new ResourceStack(Resource.MONEY, 800 * scale),
                    new ResourceStack(Resource.WOOD, 500 * scale),
                    new ResourceStack(Resource.METAL, 100 * scale),
                    new ResourceStack(Resource.STONE, 300 * scale)
            );
            case TOWN -> List.of(
                    new ResourceStack(Resource.MONEY, 200 * scale),
                    new ResourceStack(Resource.WOOD, 120 * scale),
                    new ResourceStack(Resource.METAL, 20 * scale),
                    new ResourceStack(Resource.STONE, 60 * scale)
            );
            default -> List.of();
        };
    }
}
