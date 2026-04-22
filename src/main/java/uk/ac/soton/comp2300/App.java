package uk.ac.soton.comp2300;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;


import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.event.NotificationLogic;
import uk.ac.soton.comp2300.event.RefreshVisuals;
import uk.ac.soton.comp2300.model.*;
import uk.ac.soton.comp2300.model.energy.EnergyLabel;
import uk.ac.soton.comp2300.model.game_logic.*;
import uk.ac.soton.comp2300.scene.LoginScene;
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
    private Timeline gameClock;
    private MainWindow mainWindow;

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
    private final CookieStorageService cookieStorageService = new CookieStorageService();

    public static void main(String[] args) {
        logger.info("Starting client");
        launch();
    }

    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;

        // This prevents the app from closing when minimized,
        // but requires manual handling for the 'X' button.

        stage.setOnCloseRequest(e -> {
            logger.info("Closing application...");

            try {
                stop(); // save + shutdown logic
            } catch (Exception ex) {
                logger.error("Error during shutdown: " + ex.getMessage());
            }

            Platform.exit();
            System.exit(0);
        });

        setupNotificationLogic();
        setupGameLogic();
        open();

        startGameClock();

    }
    /**Starts the in game timer running, calls time sensitive methods**/
    private void startGameClock() {
        gameClock = new Timeline(
                new KeyFrame(javafx.util.Duration.millis(200), event ->{
                    if (gameController != null) {
                        gameController.gameLoopTick();
                    }

                    if (mainWindow != null && mainWindow.getCurrentScene() instanceof RefreshVisuals refresh) {
                        refresh.refreshVisuals();
                    }
                })




        );
        gameClock.setCycleCount(Timeline.INDEFINITE);
        gameClock.play();
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
        applyClaimedTaskState(currentSessionTasks);
        return currentSessionTasks;
    }

    private void applyClaimedTaskState(List<Task> tasks) {
        var claimedToday = cookieStorageService.getClaimedTasksToday();
        for (Task task : tasks) {
            task.setRewardCollected(claimedToday.contains(task.getId()));
        }
    }

    public void incrementCompletedTasks() {

        this.completedScheduledTasks++;
        logger.info("Total completed tasks: " + completedScheduledTasks);
    }

    public int getCompletedScheduledTasks() {
        if (repository == null) {
            return completedScheduledTasks;
        }

        int completedNotifications = (int) repository.getAllNotifications().stream()
                .filter(note -> note.getStatus() == Notification.Status.TASK_COMPLETED)
                .count();

        if (completedNotifications > completedScheduledTasks) {
            completedScheduledTasks = completedNotifications;
        }

        return completedScheduledTasks;
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
        this.mainWindow = new MainWindow(stage, width, height);
        mainWindow.loadScene(new LoginScene(mainWindow));
        stage.show();
    }

    @Override
    public void stop() {


        logger.info("Saving and stopping background threads...");
        try {
            if (notificationLogic != null) {
                notificationLogic.shutdown();
            }
            if (gameClock != null) {
                gameClock.stop();
            }
            if (saveManager != null && gameState != null) {
                saveManager.saveGame(gameState);
            }
        } catch (Exception e) {
            logger.error("Stop sequence encountered an error: " + e.getMessage());

        }
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

    public CookieStorageService getCookieStorageService() {
        return cookieStorageService;
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
                String rewardMessage = giveReward(levelBefore, levelAfter);

                triggerLevelUpNotification(levelAfter, rewardMessage);

            }
        }
    }

    //Finds and applies in game reward, returns message for each reward.
    private String giveReward(int levelBefore, int levelAfter) {
        Planet planet = App.getInstance().getGameController()
                .getGameState().getSelectedPlanet();

        int selector = Items.selectRewardLvl(levelAfter);
        Items rewardItem = Items.selectItem(selector);

        rewardItem.applyItem(planet);
        return rewardItem.getMessage();
    }

    private void triggerLevelUpNotification(int newLevel, String rewardMsg) {
        var levelRecord = new uk.ac.soton.comp2300.event.NotificationRecord(
                "LVL_UP_" + newLevel,
                "Level Up!",

                "Your eco-influence is growing. ⭐" + "\n" + rewardMsg , // Updated description



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


    private void rewardsSystem (int levelBefore, int levelAfter ){
        int lvlDiff = levelAfter - levelBefore;

        for (int i = 0;  i >= lvlDiff ; i++){
           int lvlReward = levelBefore + 1;
        }
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
            new java.util.Timer(true).schedule(new java.util.TimerTask() {
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
