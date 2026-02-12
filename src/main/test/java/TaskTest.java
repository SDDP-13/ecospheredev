import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.Task;
import uk.ac.soton.comp2300.model.ResourceStack;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Expanded test suite for Task logic and App resource integration.
 */
public class TaskTest {
    private App app;
    private Task multiResourceTask;
    private Task emptyTask;

    @BeforeEach
    void setUp() {
        app = new App();

        // A task that gives all three resource types
        multiResourceTask = new Task("Full Reward", "Description", List.of(
                new ResourceStack(Resource.MONEY, 100),
                new ResourceStack(Resource.WOOD, 50),
                new ResourceStack(Resource.METAL, 20)
        ));

        // A task with no rewards to test edge cases
        emptyTask = new Task("Empty", "No rewards", new ArrayList<>());
    }

    @Test
    void testMultiResourceClaim() {
        // Claim the task once
        multiResourceTask.toggleRewardCollected();

        // Manually trigger the resource addition loop like the UI does
        for (ResourceStack stack : multiResourceTask.getRewards()) {
            app.addResources(stack.getType(), stack.getAmount());
        }

        // Verify all three vaults updated correctly
        assertAll("Resource Totals",
                () -> assertEquals(100, app.getMoney(), "Money should be 100"),
                () -> assertEquals(50, app.getWood(), "Wood should be 50"),
                () -> assertEquals(20, app.getMetal(), "Metal should be 20")
        );
    }

    @Test
    void testEmptyTaskClaim() {
        // Ensure the app doesn't crash if a task has zero rewards
        emptyTask.toggleRewardCollected();

        for (ResourceStack stack : emptyTask.getRewards()) {
            app.addResources(stack.getType(), stack.getAmount());
        }

        assertTrue(emptyTask.getRewardCollected(), "Task should still be marked done");
        assertEquals(0, app.getMoney(), "Resources should stay at 0");
    }

    @Test
    void testResourcePersistence() {
        // Test that adding resources multiple times (different tasks) accumulates correctly
        app.addResources(Resource.MONEY, 100);
        app.addResources(Resource.MONEY, 50);

        assertEquals(150, app.getMoney(), "Money should accumulate (100 + 50)");
    }
}