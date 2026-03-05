package uk.ac.soton.comp2300;

import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.event.NotificationLogic;
import uk.ac.soton.comp2300.model.*;
import uk.ac.soton.comp2300.model.game_logic.*;
import uk.ac.soton.comp2300.scene.LoginScene;
import uk.ac.soton.comp2300.scene.MenuScene;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.ArrayList;
import java.util.List;


public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);

    private final int width = 450;
    private final int height = 800;

    //private int totalXp = 0;
    private GameState gameState;
    private GameController gameController;
    private GameSaveManager saveManager;
    private GameLoadManager loadManager;

    private int completedScheduledTasks = 0;

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
        setupGameLogic();
        open();
    }
    public int getTotalXp() {
        if (gameState == null) return 0;
        return gameState.getTotalXp();
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

    public void incrementCompletedTasks() {
        this.completedScheduledTasks++;
        logger.info("Total completed tasks: " + completedScheduledTasks);
    }

    public int getCompletedScheduledTasks() {
        if (currentSessionTasks == null) return 0;

        int count = 0;
        for (Task task : currentSessionTasks) {
            if (task.getRewardCollected()) {
                count++;
            }
        }
        return count;
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

    private void setupGameLogic() {
        saveManager = new GameSaveManager();
        loadManager = new GameLoadManager();

        GameState loadedState = loadManager.loadGame();
        if (loadedState != null) {
            this.gameState = loadedState;
            gameController = new GameController(gameState);

        } else {
            this.gameState = new GameState();
            gameController = new GameController(gameState);
            gameController.initializeNewGame();
        }
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

        saveManager.saveGame(gameState);
    }

    public static App getInstance() {
        return instance;
    }

    public GameController getGameController() { return gameController; }

    public NotificationRepository getRepository() {
        return this.repository;
    }

    public NotificationLogic getNotificationLogic() {
        return this.notificationLogic;
    }

    // minor patch to sync with Game state
    public int getMoney() { return gameController.getGameState().getResourceAmount(Resource.MONEY); }
    public int getMetal() { return gameController.getGameState().getResourceAmount(Resource.METAL); }
    public int getWood() { return gameController.getGameState().getResourceAmount(Resource.WOOD); }

    public void addResources(uk.ac.soton.comp2300.model.Resource type, int amount) {
        if (type == null) return;
        if (gameController == null) {System.out.println("[WARNING] game controller not exists"); return;}
        System.out.println("Updated resources | M:" + getMoney() + " W:" + getWood() + " Met:" + getMetal());
    }
    public double getEnergySavedForDevice(String deviceName) {
        if (deviceName == null) return 0.5; // Default value for "Other"

        return switch (deviceName.toLowerCase()) {
            case "washing machine" -> 1.2;
            case "dishwasher" -> 1.5;
            case "dryer" -> 2.5;
            case "radiator" -> 3.0;
            case "air conditioner" -> 4.5;
            case "tv" -> 0.3;
            case "garden lights" -> 0.8;
            default -> 0.5;
        };
    }
    private double totalEnergySaved = 0.0;

    public double getTotalEnergySaved() {
        return totalEnergySaved;
    }

    public void addEnergySavings(String deviceName) {
        double saved = switch (deviceName.toLowerCase()) {
            case "washing machine" -> 1.2;
            case "dishwasher" -> 1.5;
            case "dryer" -> 2.5;
            case "radiator" -> 3.0;
            case "air conditioner" -> 4.5;
            case "tv" -> 0.3;
            case "garden lights" -> 0.8;
            default -> 0.5; // For "Other" devices
        };
        this.totalEnergySaved += saved;
    }

    // Add to App.java
    private double totalCo2Saved = 0.0;
    private double totalMoneySaved = 0.0;

    public double getTotalCo2Saved() { return totalCo2Saved; }
    public double getTotalMoneySaved() { return totalMoneySaved; }

    /**
     * Updates global session totals using a report from the EcoSavingsService.
     */
    public void addReportSavings(EcoSavingsReport report) {
        this.totalCo2Saved += report.getCo2SavedKg();
        this.totalMoneySaved += report.getMoneySavedPounds();
    }
    /**
     * Helper to calculate current level and progress based on total XP.
     * Returns an array: [Current Level, XP in Current Level, XP Required for Next Level, Progress Ratio]
     */
    public double[] getLevelData() {
        int totalXp = getTotalXp();
        int level = 0;
        int xpAtStartOfCurrentLevel = 0;

        // Logic: Level 1 @ 200, Level 2 @ 600, etc.
        while (totalXp >= xpAtStartOfCurrentLevel + (200 * (level + 1))) {
            xpAtStartOfCurrentLevel += (200 * (level + 1));
            level++;
        }

        int xpInCurrentLevel = totalXp - xpAtStartOfCurrentLevel;
        int xpRequiredForThisLevel = 200 * (level + 1);
        double progressRatio = (double) xpInCurrentLevel / xpRequiredForThisLevel;

        return new double[]{level, xpInCurrentLevel, xpRequiredForThisLevel, progressRatio};
    }
    public void addXp(int amount) {
        if (gameState != null) {
            // 1. Get current level before adding XP
            int levelBefore = (int) getLevelData()[0];

            // 2. Add the XP to the saveable state
            gameState.addXp(amount);
            logger.info("XP increased! New Total: " + gameState.getTotalXp());

            // 3. Get level after adding XP
            int levelAfter = (int) getLevelData()[0];

            // 4. Trigger popup if level increased
            if (levelAfter > levelBefore) {
                triggerLevelUpNotification(levelAfter);
            }
        }
    }

    private void triggerLevelUpNotification(int newLevel) {
        var levelRecord = new uk.ac.soton.comp2300.event.NotificationRecord(
                "LVL_UP_" + newLevel,
                "Level Up!",
                "Your eco-influence is growing. ⭐", // Updated description
                java.time.LocalDateTime.now(),
                uk.ac.soton.comp2300.model.Notification.Type.GAME_EVENT // Type is GAME_EVENT
        );

        if (notificationLogic != null && notificationLogic.getListener() != null) {
            notificationLogic.getListener().onNotificationSent(levelRecord);
        }
    }
}