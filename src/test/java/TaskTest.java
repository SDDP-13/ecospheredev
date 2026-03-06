import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.Task;
import uk.ac.soton.comp2300.model.ResourceStack;
import uk.ac.soton.comp2300.model.game_logic.GameController;
import uk.ac.soton.comp2300.model.game_logic.GameState;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private App app;

    @BeforeAll
    static void setUpApp() throws Exception {
        // Initialize the App singleton via reflection to avoid NullPointer on getInstance()
        App newApp = new App();
        Field inst = App.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, newApp);
    }

    @BeforeEach
    void setUp() throws Exception {
        app = App.getInstance();

        // Initialize GameState and GameController
        GameState state = new GameState();
        GameController controller = new GameController(state);

        // Inject the initialized objects into the app instance via reflection
        Field gameStateField = App.class.getDeclaredField("gameState");
        gameStateField.setAccessible(true);
        gameStateField.set(app, state);

        Field controllerField = App.class.getDeclaredField("gameController");
        controllerField.setAccessible(true);
        controllerField.set(app, controller);
    }

    @Test
    void testTaskRewardAndXpClaim() {
        // Define a task locally to avoid 'Cannot resolve symbol' errors
        Task task = new Task("Build a structure (1)", "Test Desc", List.of(
                new ResourceStack(Resource.MONEY, 100)
        ));

        // Initial values
        int initialTotalXp = app.getTotalXp();
        int initialMoney = app.getMoney();

        // Execute claim logic exactly as TaskScene.java does
        task.toggleRewardCollected();
        app.addXp(100);

        for (ResourceStack stack : task.getRewards()) {
            app.getGameController().addResource(stack.getType(), stack.getAmount());
        }

        // Assertions
        assertTrue(task.getRewardCollected(), "Task should be marked collected");
        assertEquals(initialTotalXp + 100, app.getTotalXp(), "Total lifetime XP should be 100");
        assertEquals(initialMoney + 100, app.getMoney(), "Resources should be added to GameState");
    }

    @Test
    void testTaskLockingLogic() {
        // Logic for Task 1: Locked if completedCount < 1
        Task task1 = new Task("Did a scheduled task (1)", "Desc", List.of());

        // App.getCompletedScheduledTasks iterates through currentSessionTasks
        // Since we haven't set session tasks, this should be 0 by default
        int completedCount = app.getCompletedScheduledTasks();

        boolean isLocked = (task1.getId().equals("Did a scheduled task (1)") && completedCount < 1);
        assertTrue(isLocked, "Task 1 should be locked when completed count is 0");
    }
}