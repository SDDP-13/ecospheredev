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
        BuildingData building = controller.placeBuidling(testPlanet, testBuildType, testTheta - 1, testPhi - 1);
        assertNotNull(building);
        assertEquals(1, testPlanet.getBuildingData().size());
    }

    @Test
    void testPlaceMultipleBuildings() {
        BuildingData first = controller.placeBuidling(testPlanet, testBuildType, testTheta - 1, testPhi - 1);
        assertNotNull(first);
        assertEquals(1, testPlanet.getBuildingData().size());

        BuildingData second = controller.placeBuidling(testPlanet, testBuildType, testTheta, testPhi);
        assertNotNull(second);
        assertEquals(2, testPlanet.getBuildingData().size());

        BuildingData third = controller.placeBuidling(testPlanet, testBuildType, testTheta + 1, testPhi + 1);
        assertNotNull(third);
        assertEquals(3, testPlanet.getBuildingData().size());
    }

    @Test
    void testBuildingDataStoredCorrectly() {

        BuildingData building = controller.placeBuidling(testPlanet, testBuildType, testTheta, testPhi);

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

        controller.placeBuidling(testPlanet, testBuildType, testTheta, testPhi);

        double closeTheta = testTheta + Math.toRadians(2);

        boolean result = controller.isBuildLocationFree(testPlanet, closeTheta, testPhi);

        assertFalse(result);
    }

    @Test
    void testLocationFarEnough() {

        controller.placeBuidling(testPlanet, testBuildType, testTheta, testPhi);

        double farTheta = testTheta + Math.toRadians(7);

        boolean result = controller.isBuildLocationFree(testPlanet, farTheta, testPhi);

        assertTrue(result);
    }


    @Test
    void testInvalidPlacementNoAddedBuilding() {

        controller.placeBuidling(testPlanet, testBuildType, testTheta, testPhi);

        int before = testPlanet.getBuildingData().size();

        controller.placeBuidling(testPlanet, testBuildType, testTheta, testPhi);

        int after = testPlanet.getBuildingData().size();

        assertEquals(before, after);
    }
    @Test
    void testBuildingDifferentiation() {
        // Place a Town
        controller.placeBuidling(testPlanet, BuildingType.TOWN, 1.0, 1.0);
        // Place a Lumber Mill
        controller.placeBuidling(testPlanet, BuildingType.LUMBER_MILL, 2.0, 2.0);

        // Verify filter logic separates them
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
