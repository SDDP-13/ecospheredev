import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.NotificationRepository;
import uk.ac.soton.comp2300.model.ScheduleManager;
import uk.ac.soton.comp2300.model.ScheduleTask;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleTest {

    @BeforeAll
    static void setUpApp() throws Exception {
        App app = new App();

        Field inst = App.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, app);

        NotificationRepository repo = new NotificationRepository() {
            private final List<Notification> notifications = new ArrayList<>();

            @Override public List<Notification> getAllNotifications() { return notifications; }
            @Override public void saveChanges(Notification n) { }
            @Override public void deleteNotification(Notification n) { }
            @Override public void clearNotifications() { notifications.clear(); }
            @Override public void add(Notification n) { notifications.add(n); }
        };

        Field repoField = App.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(app, repo);

    }

    @BeforeEach
    void taskClear() {
        ScheduleManager.getTasks().clear();
    }

    @Test
    void testAddTask() {
        ScheduleTask task = new ScheduleTask(
                "Washing Machine",
                LocalTime.of(15,0),
                "mock description"
        );

        boolean result = ScheduleManager.addTask(task);
        assertTrue(result);
        assertEquals(1, ScheduleManager.getTasks().size());
        assertEquals("Washing Machine",
                ScheduleManager.getTasks().get(0).getDeviceName());
    }

    @Test
    void testPreventDuplicateTask() {
        ScheduleTask task1 = new ScheduleTask(
                "TV",
                LocalTime.of(10,0),
                "mock description"
        );

        ScheduleTask task2 = new ScheduleTask(
                "TV",
                LocalTime.of(10,0),
                "mock description"
        );

        ScheduleManager.addTask(task1);
        boolean result = ScheduleManager.addTask(task2);

        assertFalse(result);
        assertEquals(1, ScheduleManager.getTasks().size());
    }

    @Test
    void testSameDeviceDifferentTime() {
        ScheduleTask task1 = new ScheduleTask(
                "TV",
                LocalTime.of(10,0),
                "mock description"
        );
        ScheduleTask task2 = new ScheduleTask(
                "TV",
                LocalTime.of(12,0),
                "mock description"
        );

        ScheduleManager.addTask(task1);
        boolean result = ScheduleManager.addTask(task2);

        assertTrue(result);
        assertEquals(2, ScheduleManager.getTasks().size());
    }

    @Test
    void testEditTask() {
        ScheduleTask task = new ScheduleTask(
                "Dryer",
                LocalTime.of(10,0),
                "mock description"
        );

        ScheduleManager.addTask(task);

        boolean result = ScheduleManager.updateTask(
                task,
                "Dryer",
                LocalTime.of(12,0),
                "new description"
        );

        assertTrue(result);
        assertEquals(LocalTime.of(12,0), task.getTime());
        assertEquals("new description", task.getDescription());
    }

    @Test
    void testRemoveTask() {
        ScheduleTask task = new ScheduleTask(
                "TV",
                LocalTime.of(10,0),
                "mock description"
        );

        ScheduleManager.addTask(task);
        ScheduleManager.removeTask(task);

        assertEquals(0, ScheduleManager.getTasks().size());
    }
}
