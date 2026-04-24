

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.soton.comp2300.model.game_logic.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression Test for Building Logic.
 * Verifies placement, collision detection, and type differentiation.
 */
public class BuildTest {

    private GameController controller;
    private Planet testPlanet;
    private final double testTheta = 1.0;
    private final double testPhi = 1.0;
    private final BuildingType testBuildType = BuildingType.TOWN;

    @BeforeEach
    void setUpVar() {
        // Initializing the GameController with a fresh state and planet for each test
        controller = new GameController(new GameState());
        testPlanet = new Planet("Test");
    }

    @Test
    void testPlaceSingleBuilding() {
        BuildingData building = controller.placeBuildingTest(testPlanet, testBuildType, testTheta - 1, testPhi - 1);
        assertNotNull(building, "Building should be successfully created");
        assertEquals(1, testPlanet.getBuildingData().size(), "Planet should contain exactly one building");
    }

    @Test
    void testPlaceMultipleBuildings() {
        // Placing first building
        controller.placeBuildingTest(testPlanet, testBuildType, testTheta - 1, testPhi - 1);
        assertEquals(1, testPlanet.getBuildingData().size());

        // Placing second building at different coordinates
        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);
        assertEquals(2, testPlanet.getBuildingData().size());

        // Placing third building
        controller.placeBuildingTest(testPlanet, testBuildType, testTheta + 1, testPhi + 1);
        assertEquals(3, testPlanet.getBuildingData().size());
    }

    @Test
    void testBuildingDataStoredCorrectly() {
        BuildingData building = controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        assertNotNull(building);
        assertEquals(testBuildType, building.getType(), "Stored type should match input");
        assertEquals(testTheta, building.getTheta(), "Stored Theta should match input");
        assertEquals(testPhi, building.getPhi(), "Stored Phi should match input");
    }

    @Test
    void testLocationFreeEmptyPlanet() {
        // A fresh planet should return true for any valid coordinate
        boolean result = controller.isBuildLocationFree(testPlanet, testTheta, testPhi);
        assertTrue(result, "Location should be free on an empty planet");
    }

    @Test
    void testLocationTooClose() {
        // Place a building as an obstacle
        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        // Attempting to place a building only 2 degrees away (Too close)
        double closeTheta = testTheta + Math.toRadians(2);
        boolean result = controller.isBuildLocationFree(testPlanet, closeTheta, testPhi);

        assertFalse(result, "Location should be blocked by existing building");
    }

    @Test
    void testLocationFarEnough() {
        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        // Attempting to place a building 7 degrees away (Sufficient distance)
        double farTheta = testTheta + Math.toRadians(7);
        boolean result = controller.isBuildLocationFree(testPlanet, farTheta, testPhi);

        assertTrue(result, "Location should be free if far enough from existing buildings");
    }

    @Test
    void testInvalidPlacementNoAddedBuilding() {
        // Placing a building at the same spot twice
        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);
        int before = testPlanet.getBuildingData().size();

        // Second attempt at the exact same coordinates should fail
        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);
        int after = testPlanet.getBuildingData().size();

        assertEquals(before, after, "Planet size should not increase for invalid placement");
    }

    @Test
    void testBuildingDifferentiation() {
        controller.placeBuildingTest(testPlanet, BuildingType.TOWN, 1.0, 1.0);
        controller.placeBuildingTest(testPlanet, BuildingType.LUMBER_MILL, 2.0, 2.0);

        long townCount = testPlanet.getBuildingData().stream()
                .filter(b -> b.getType() == BuildingType.TOWN)
                .count();
        long millCount = testPlanet.getBuildingData().stream()
                .filter(b -> b.getType() == BuildingType.LUMBER_MILL)
                .count();

        assertEquals(1, townCount, "Town count should be independent");
        assertEquals(1, millCount, "Lumber Mill count should be independent");
    }
}