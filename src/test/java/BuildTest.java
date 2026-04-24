import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.soton.comp2300.model.game_logic.*;

import static org.junit.jupiter.api.Assertions.*;



public class BuildTest {

    GameController controller;
    Planet testPlanet;
    double testTheta = 1.0;
    double testPhi = 1.0;
    BuildingType testBuildType = BuildingType.TOWN;


    @BeforeEach
    void setUpVar() {
        controller = new GameController(new GameState());
        testPlanet = new Planet("Test");

    }

    @Test
    void testPlaceSingleBuilding() {
        BuildingData building = controller.placeBuildingTest(testPlanet, testBuildType, testTheta - 1, testPhi - 1);
        assertNotNull(building);
        assertEquals(1, testPlanet.getBuildingData().size());
    }

    @Test
    void testPlaceMultipleBuildings() {
        BuildingData first = controller.placeBuildingTest(testPlanet, testBuildType, testTheta - 1, testPhi - 1);
        assertNotNull(first);
        assertEquals(1, testPlanet.getBuildingData().size());

        BuildingData second = controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);
        assertNotNull(second);
        assertEquals(2, testPlanet.getBuildingData().size());

        BuildingData third = controller.placeBuildingTest(testPlanet, testBuildType, testTheta + 1, testPhi + 1);
        assertNotNull(third);
        assertEquals(3, testPlanet.getBuildingData().size());
    }

    @Test
    void testBuildingDataStoredCorrectly() {

        BuildingData building = controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        assertNotNull(building);
        assertEquals(testBuildType, building.getType());
        assertEquals(testTheta, building.getTheta());
        assertEquals(testPhi, building.getPhi());
    }

    @Test
    void testLocationFreeEmptyPlanet() {

        boolean result = controller.isBuildLocationFree(testPlanet, testTheta, testPhi);

        assertTrue(result);
    }

    @Test
    void testLocationTooClose() {

        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        double closeTheta = testTheta + Math.toRadians(2);

        boolean result = controller.isBuildLocationFree(testPlanet, closeTheta, testPhi);

        assertFalse(result);
    }

    @Test
    void testLocationFarEnough() {

        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        double farTheta = testTheta + Math.toRadians(7);

        boolean result = controller.isBuildLocationFree(testPlanet, farTheta, testPhi);

        assertTrue(result);
    }


    @Test
    void testInvalidPlacementNoAddedBuilding() {

        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        int before = testPlanet.getBuildingData().size();

        controller.placeBuildingTest(testPlanet, testBuildType, testTheta, testPhi);

        int after = testPlanet.getBuildingData().size();

        assertEquals(before, after);
    }
}
