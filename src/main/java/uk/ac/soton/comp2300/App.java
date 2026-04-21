package uk.ac.soton.comp2300;

import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.event.NotificationLogic;
import uk.ac.soton.comp2300.model.*;
import uk.ac.soton.comp2300.model.energy.EnergyLabel;
import uk.ac.soton.comp2300.model.game_logic.*;
import uk.ac.soton.comp2300.scene.LoginScene;
import uk.ac.soton.comp2300.scene.MenuScene;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private final EcoSavingsService ecoSavingsService = new EcoSavingsService();
    private final Map<String, EcoSavingsReport> savingsReportCache = new HashMap<>();
    private final Map<String, Double> dailySavingsMap = new HashMap<>();

    private final Map<String, Double> dailyTaskCompletionMap = new HashMap<>();

    public static void main(String[] args) {
        logger.info("Starting client");
        launch();
    }

    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;

        // Prevents the app from shutting down background tasks when minimized
        javafx.application.Platform.setImplicitExit(false);

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
    /** Call this when a task from the TASK SCENE is completed */
    /** Call this when a task from the TASK SCENE is completed */
    public void addTaskCompletion() {
        String today = LocalDate.now().toString();
        double current = dailyTaskCompletionMap.getOrDefault(today, 0.0);

        // Increase by 1
        dailyTaskCompletionMap.put(today, current + 1.0);
    }

    public Map<String, Double> getDailyTaskCompletionMap() {
        return dailyTaskCompletionMap;
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
            javafx.application.Platform.runLater(() -> {
                showSystemNotification(record.title(), record.message());
            });
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

    public EcoSavingsReport getSavingsReportForTask(ScheduleTask task) {
        if (task == null || task.getDeviceName() == null || task.getDeviceName().isBlank()) {
            return buildFallbackReport("Other");
        }

        LocalTime time = task.getTime() != null ? task.getTime() : LocalTime.now().withSecond(0).withNano(0);
        Duration duration = task.getDuration() != null ? task.getDuration() : Duration.ofHours(1);
        LocalDate todayUk = LocalDate.now(ZoneId.of("Europe/London"));
        String cacheKey = (task.getDeviceName() + "|" + time + "|" + duration.toMinutes() + "|" + todayUk).toLowerCase();

        EcoSavingsReport cached = savingsReportCache.get(cacheKey);
        System.out.println("Cached?" + (cached != null));
        if (cached != null) return cached;

        try {
            ScheduleTask normalizedTask = new ScheduleTask(task.getDeviceName(), time, duration, task.getDescription());
            EcoSavingsReport report = ecoSavingsService.calculate(normalizedTask, EnergyLabel.F);
            savingsReportCache.put(cacheKey, report);
            return report;
        } catch (Exception e) {
            logger.warn("Using fallback savings for device '{}': {}", task.getDeviceName(), e.getMessage());
            EcoSavingsReport fallback = buildFallbackReport(task.getDeviceName());
            savingsReportCache.put(cacheKey, fallback);
            return fallback;
        }
    }

    public EcoSavingsReport getSavingsReportForDevice(String deviceName) {
        ScheduleTask defaultTask = new ScheduleTask(
                deviceName == null ? "Other" : deviceName,
                LocalTime.now().withSecond(0).withNano(0),
                Duration.ofHours(1),
                "Auto generated"
        );
        return getSavingsReportForTask(defaultTask);
    }

    public double getEnergySavedForDevice(String deviceName) {
        EcoSavingsReport report = getSavingsReportForDevice(deviceName);
        double energyEquivalent = report.getMoneySavedPounds() / 0.15;
        return Math.max(0.0, energyEquivalent);
    }
    private double totalEnergySaved = 0.0;

    public double getTotalEnergySaved() {
        return totalEnergySaved;
    }

    public void addEnergySavings(String deviceName) {
        this.totalEnergySaved += getEnergySavedForDevice(deviceName);
    }

    private EcoSavingsReport buildFallbackReport(String deviceName) {
        double energy = legacyEnergySavedForDevice(deviceName);
        return new EcoSavingsReport(energy * 0.15, energy * 0.2);
    }

    private double legacyEnergySavedForDevice(String deviceName) {
        if (deviceName == null) return 0.5;

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

    // Add to App.java
    private double totalCo2Saved = 0.0;
    private double totalMoneySaved = 0.0;

    public double getTotalCo2Saved() { return totalCo2Saved; }
    public double getTotalMoneySaved() { return totalMoneySaved; }

    /**
     * Updates global session totals using a report from the EcoSavingsService.
     */
    /**
     * Updates global session totals and tracks completion for dashboard charts.
     */
    public void addReportSavings(EcoSavingsReport report) {
        if (report == null) return;

        double moneySaved = Math.max(0.0, report.getMoneySavedPounds());
        double co2Saved = Math.max(0.0, report.getCo2SavedKg());
        double energySaved = moneySaved / 0.15;

        this.totalMoneySaved += moneySaved;
        this.totalCo2Saved += co2Saved;
        this.totalEnergySaved += energySaved;

        // 1. Increment the counter for the bottom Weekly Progress bar
        this.completedScheduledTasks++;

        // 2. Increment the value for today's bar in the Task Chart
        //String today = LocalDate.now().toString(); // e.g., "2026-04-20"
        //double currentDayTotal = dailySavingsMap.getOrDefault(today, 0.0);

        // We add a value (e.g., 20.0) so the bar grows by 20% per task completed
        //dailySavingsMap.put(today, currentDayTotal + 20.0);

        //logger.info("Task completed! Day total for " + today + " is now: " + dailySavingsMap.get(today));
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
                "Your eco-influence is growing. ⭐",
                java.time.LocalDateTime.now(),
                uk.ac.soton.comp2300.model.Notification.Type.GAME_EVENT
        );

        if (notificationLogic != null && notificationLogic.getListener() != null) {
            notificationLogic.getListener().onNotificationSent(levelRecord);
        }

    }
    public Map<String, Double> getDailySavingsMap() {
        return dailySavingsMap;
    }

    public void showSystemNotification(String title, String message) {
        // Check if the OS allows tray icons
        if (!java.awt.SystemTray.isSupported()) {
            logger.warn("SystemTray not supported on this OS");
            return;
        }

        try {
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

            java.net.URL imageLoc = getClass().getResource("/images/Coin.png");
            java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().getImage(imageLoc);

            java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image, "Ecosphere");
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);

            // Trigger the outside of the software notification
            trayIcon.displayMessage(title, message, java.awt.TrayIcon.MessageType.INFO);

            // Remove the icon after 5 seconds so they don't clutter the taskbar
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override public void run() { tray.remove(trayIcon); }
            }, 5000);

        } catch (Exception e) {
            logger.error("Failed to trigger Windows notification: " + e.getMessage());
        }
    }
    public int getBuildingsPlacedCount() {
        if (gameState == null) return 0;
        return gameState.getBuildingsPlaced(); //
    }
}
