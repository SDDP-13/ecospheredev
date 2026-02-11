package uk.ac.soton.comp2300;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.event.NotificationLogic;
import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.NotificationRepository;
import uk.ac.soton.comp2300.scene.LoginScene;
import uk.ac.soton.comp2300.scene.MenuScene;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);

    private final int width = 450;
    private final int height = 800;

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

    private void setupNotificationLogic() {
        this.repository = new NotificationRepository() {
            private final List<Notification> notifications = new ArrayList<>();
            @Override public List<Notification> getAllNotifications() { return notifications; }
            @Override public void saveChanges(Notification n) { }
            @Override public void deleteNotification(Notification n) { }
            @Override public void clearNotifications() { notifications.clear(); }
            @Override public void add(Notification n) { notifications.add(n); }
        };

        // --- ADD THIS TEST DATA ---
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Notification testNote = new Notification(
                Notification.Source.SYSTEM,
                Notification.Type.ENERGY_ALERT,
                "Dish Washer",
                "Task: Run Dish Washer\nReward: Money 10",
                now, // scheduled_Time
                now, // sendAt (Send it immediately)
                "ref-123"
        );
        repository.add(testNote);
        // ---------------------------

        this.notificationLogic = new NotificationLogic(repository, record -> {
            logger.info("New notification sent: " + record.title());
            // If you are currently on the NotificationScene, it will catch this via the interface
        });

        notificationLogic.start();
    }

    public void open() {
        logger.info("Opening window at " + width + "x" + height);
        var mainWindow = new MainWindow(stage, width, height);

        // change between MenuScene and LoginScene for which you want to appear first
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
}
