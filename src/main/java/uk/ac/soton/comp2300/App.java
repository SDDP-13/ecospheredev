package uk.ac.soton.comp2300;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.event.NotificationLogic;
import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.NotificationRepository;
import uk.ac.soton.comp2300.model.Task;
import uk.ac.soton.comp2300.model.TaskPool;
import uk.ac.soton.comp2300.scene.LoginScene;
import uk.ac.soton.comp2300.scene.MenuScene;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);

    private final int width = 450;
    private final int height = 800;

    private int money = 0;
    private int metal = 0;
    private int wood = 0;

    // Persistent storage for the current session
    private List<Task> currentSessionTasks;
    private final TaskPool taskPool = new TaskPool();

    private static App instance;
    private Stage stage;

    private NotificationLogic notificationLogic;
    private NotificationRepository repository;

    public static void main(String[] args) {
        logger.info("Starting client");
        launch();
    }

    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;

        setupNotificationLogic();
        open();
    }

    /**
     * Retrieves the daily tasks. If they don't exist yet, they are generated once.
     * This ensures the "claimed" state is preserved during the session.
     */
    public List<Task> getTasks() {
        if (currentSessionTasks == null) {
            currentSessionTasks = taskPool.generateDailyTasks();
        }
        return currentSessionTasks;
    }

    private void setupNotificationLogic() {
        this.repository = new NotificationRepository() {
            private final List<Notification> notifications = new ArrayList<>();
            @Override public List<Notification> getAllNotifications() { return notifications; }
            @Override public void saveChanges(Notification n) { }
            @Override public void deleteNotification(Notification n) { }
            @Override public void clearNotifications() { notifications.clear(); }
            @Override public void add(Notification n) { notifications.add(n); }
        };

        this.notificationLogic = new NotificationLogic(repository, record -> {
            logger.info("New notification sent: " + record.title());
        });

        notificationLogic.start();
    }

    public void open() {
        logger.info("Opening window at " + width + "x" + height);
        var mainWindow = new MainWindow(stage, width, height);
        mainWindow.loadScene(new LoginScene(mainWindow));
        stage.show();
    }

    @Override
    public void stop() {
        logger.info("Shutting down");
        if (notificationLogic != null) {
            notificationLogic.shutdown();
        }
    }

    public static App getInstance() {
        return instance;
    }

    public NotificationRepository getRepository() {
        return this.repository;
    }

    public NotificationLogic getNotificationLogic() {
        return this.notificationLogic;
    }

    public int getMoney() { return money; }
    public int getMetal() { return metal; }
    public int getWood() { return wood; }

    public void addResources(uk.ac.soton.comp2300.model.Resource type, int amount) {
        if (type == null) return;

        switch (type) {
            case MONEY -> this.money += amount;
            case WOOD -> this.wood += amount;
            case METAL -> this.metal += amount;
            default -> System.out.println("Unknown resource type: " + type);
        }
        System.out.println("Updated resources | M:" + money + " W:" + wood + " Met:" + metal);
    }
}