package uk.ac.soton.comp2300.model.game_logic;

import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.ResourceStack;

import java.util.List;

public enum BuildingType {
    LUMBER_MILL(List.of(
            new ResourceStack(Resource.MONEY, 10),
            new ResourceStack(Resource.WOOD, 25),
            new ResourceStack(Resource.METAL, 80),
            new ResourceStack(Resource.STONE, 40)
    )),
    QUARRY(List.of(
            new ResourceStack(Resource.MONEY, 1000),
            new ResourceStack(Resource.WOOD, 25),
            new ResourceStack(Resource.METAL, 800),
            new ResourceStack(Resource.STONE, 400)
    )),
    MINE(List.of(
            new ResourceStack(Resource.MONEY, 1000),
            new ResourceStack(Resource.WOOD, 25),
            new ResourceStack(Resource.METAL, 800),
            new ResourceStack(Resource.STONE, 400)
    )),
    TOWN(List.of(
            new ResourceStack(Resource.MONEY, 1000),
            new ResourceStack(Resource.WOOD, 25),
            new ResourceStack(Resource.METAL, 800),
            new ResourceStack(Resource.STONE, 400)
    )),
    MARKET(List.of(
            new ResourceStack(Resource.MONEY, 1000),
            new ResourceStack(Resource.WOOD, 25),
            new ResourceStack(Resource.METAL, 800),
            new ResourceStack(Resource.STONE, 400)
    )),
    SPACEPORT(List.of(
            new ResourceStack(Resource.MONEY, 1000),
            new ResourceStack(Resource.WOOD, 25),
            new ResourceStack(Resource.METAL, 800),
            new ResourceStack(Resource.STONE, 400)
    )),
    RESEARCH_LAB(List.of(
            new ResourceStack(Resource.MONEY, 1000),
            new ResourceStack(Resource.WOOD, 25),
            new ResourceStack(Resource.METAL, 800),
            new ResourceStack(Resource.STONE, 400)


    ));

    private final List<ResourceStack> price;

    BuildingType(List<ResourceStack> price) {
        this.price = price;
    }
    public List<ResourceStack> getPrice() {
        return price;
    }

    public boolean isUpgradeable() {
        return switch (this) {
            case TOWN, MINE, QUARRY, LUMBER_MILL -> true;
            default -> false;
        };
    }

    public List<ResourceStack> getUpgradeCost(int level) {
        return switch (this) {
            case LUMBER_MILL -> List.of(
                    new ResourceStack(Resource.MONEY, 1000 * level),
                    new ResourceStack(Resource.WOOD, 250 * level),
                    new ResourceStack(Resource.METAL, 300 * level),
                    new ResourceStack(Resource.STONE, 100 * level)
            );
            case QUARRY -> List.of(
                    new ResourceStack(Resource.MONEY, 1000 * level),
                    new ResourceStack(Resource.WOOD, 500 * level),
                    new ResourceStack(Resource.METAL, 200 * level),
                    new ResourceStack(Resource.STONE, 50 * level)
            );
            case MINE -> List.of(
                    new ResourceStack(Resource.MONEY, 1000 * level),
                    new ResourceStack(Resource.WOOD, 50 * level),
                    new ResourceStack(Resource.METAL, 150 * level),
                    new ResourceStack(Resource.STONE, 200 * level)
            );
            case TOWN -> List.of(
                    new ResourceStack(Resource.MONEY, 3000 * level),
                    new ResourceStack(Resource.WOOD, 100 * level),
                    new ResourceStack(Resource.METAL, 80 * level),
                    new ResourceStack(Resource.STONE, 100 * level)
            );
            default -> List.of();
        };
    }
}
