package uk.ac.soton.comp2300.scene;

import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.event.NotificationListenerInterface;
import uk.ac.soton.comp2300.model.ResourceStack;
import uk.ac.soton.comp2300.model.game_logic.BuildingType;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.List;
import java.util.Random;
import uk.ac.soton.comp2300.event.NotificationRecord;
import uk.ac.soton.comp2300.ui.NotificationPopup;

/** 2 imports for the build menu*/
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

/**
 * MenuScene: The central navigation hub for the EcoSphere application.
 * Focuses on Ebaa's Sprint 1 UI layout and navigation tasks.
 */
public class MenuScene extends BaseScene implements NotificationListenerInterface {

    private HBox botMenuActions;

    public MenuScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());

        var app = uk.ac.soton.comp2300.App.getInstance();
        // Access the game state to get accurate resource counts
        var state = app.getGameController().getGameState();

        Pane starField = new Pane();
        starField.getStyleClass().add("root-black");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(random.nextInt(mainWindow.getWidth()), random.nextInt(mainWindow.getHeight()), random.nextDouble() * 1.5, Color.WHITE);
            starField.getChildren().add(star);
        }

        Circle planet = new Circle(200, Color.web("#4CAF50"));
        Button planetButton = new Button();
        planetButton.setGraphic(planet);
        planetButton.setStyle("-fx-background-color: transparent;");
        planetButton.setPickOnBounds(false);
        //planetButton.setOnAction((e) -> {mainWindow.loadScene(new PlanetScene(mainWindow, state.getSelectedPlanet()));});
        StackPane planetLayer = new StackPane(planetButton);
        planetLayer.setAlignment(Pos.BOTTOM_CENTER);
        planetLayer.setPadding(new Insets(0, 0, -60, 0));
        planetLayer.setMaxWidth(Region.USE_PREF_SIZE);
        planetLayer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(planetLayer, Pos.BOTTOM_CENTER);



        VBox resourceContainer = new VBox(8);
        resourceContainer.setPadding(new Insets(15));
        resourceContainer.setAlignment(Pos.TOP_LEFT);
        resourceContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Pulling all 4 resources from the GameState model
        int gold = state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.MONEY);
        int metal = state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.METAL);
        int wood = state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.WOOD);
        int stone = state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.STONE);

        // Displaying resources with the same formatting as the Dashboard
        resourceContainer.getChildren().addAll(
                createResourceBox("Coin.png", String.format("%,d", gold), "#d4af37"),
                createResourceBox("Metal.png", String.format("%,d", metal), "#a0a0a0"),
                createResourceBox("Wood.png", String.format("%,d", wood), "#8b4513"),
                createResourceBox("Stone.png", String.format("%,d", stone), "#708090")
        );

        Label hoverLabel = new Label("");
        hoverLabel.setVisible(false);
        hoverLabel.setMouseTransparent(true);
        hoverLabel.getStyleClass().add("hover-description");

        VBox menuDrawer = new VBox(10);
        menuDrawer.setPadding(new Insets(20));
        menuDrawer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(menuDrawer, Pos.TOP_RIGHT);

        Button menuToggle = new Button("☰");
        menuToggle.setPrefSize(50, 50);
        menuToggle.getStyleClass().add("hamburger-button");

        VBox dropDownItems = new VBox(12);
        dropDownItems.setAlignment(Pos.TOP_CENTER);
        dropDownItems.setPadding(new Insets(15, 10, 15, 10));
        dropDownItems.getStyleClass().add("menu-dropdown-bg");
        dropDownItems.setVisible(false);
        dropDownItems.setManaged(false);


        Button btnNotifications = createMenuButton("Notification.png");
        Button btnDashboard = createMenuButton("Dashboard.png");
        Button btnSchedule = createMenuButton("Schedule.png");
        Button btnTasks = createMenuButton("Tasks.png");
        Button btnSettings = createMenuButton("Settings.png");
        Button btnHelp = createMenuButton("Help.png");
        Button btnSolar = createMenuButton("SolarSystem.png");
        Button btnPlanet = createMenuButton("Build.png");

        Button[] allButtons = {btnNotifications, btnDashboard, btnSchedule, btnTasks, btnSettings, btnHelp, btnSolar, btnPlanet};
        String[] descriptions = {"Notifications", "Dashboard", "Schedules", "Tasks", "Settings", "Help", "Solar System", "Planet"};

        for (int i = 0; i < allButtons.length; i++) {
            Button b = allButtons[i];
            String desc = descriptions[i];
            b.setPrefSize(45, 45);
            b.getStyleClass().add("menu-icon-button");

            b.setOnMouseEntered(e -> {
                hoverLabel.setText(desc);
                var bounds = b.localToScene(b.getBoundsInLocal());

                if (desc.equals("Solar System")) {
                    hoverLabel.setLayoutX(bounds.getMinX());
                    hoverLabel.setLayoutY(bounds.getMinY() - 40);
                } else if (desc.equals("Planet")) {
                    hoverLabel.setLayoutX(bounds.getMinX() - 10);
                    hoverLabel.setLayoutY(bounds.getMinY() - 40);
                } else {
                    // Position for side drawer buttons
                    hoverLabel.setLayoutX(bounds.getMinX() - 95);
                    hoverLabel.setLayoutY(bounds.getMinY() + 10);
                }
                hoverLabel.setVisible(true);
            });
            b.setOnMouseExited(e -> hoverLabel.setVisible(false));
        }

        btnSettings.setOnAction(e -> mainWindow.loadScene(new SettingsScene(mainWindow)));
        btnNotifications.setOnAction(e -> mainWindow.loadScene(new NotificationScene(mainWindow)));
        btnDashboard.setOnAction(e -> mainWindow.loadScene(new DashboardScene(mainWindow)));
        btnSchedule.setOnAction(e -> mainWindow.loadScene(new ScheduleScene(mainWindow)));
        btnTasks.setOnAction(e -> mainWindow.loadScene(new TaskScene(mainWindow)));
        btnHelp.setOnAction(e -> mainWindow.loadScene(new HelpScene(mainWindow)));

        /** Logic for the bottom menu buttons*/
        btnSolar.setOnAction(e -> mainWindow.loadScene(new SolarSystemScene(mainWindow)));
        btnPlanet.setOnAction((e) -> {mainWindow.loadScene(new PlanetScene(mainWindow));});;

        menuToggle.setOnAction(e -> {
            boolean visible = !dropDownItems.isVisible();
            dropDownItems.setVisible(visible);
            dropDownItems.setManaged(visible);
        });

        dropDownItems.getChildren().addAll(btnNotifications, btnDashboard, btnSchedule, btnTasks, btnSettings, btnHelp);
        menuDrawer.getChildren().addAll(menuToggle, dropDownItems);

        botMenuActions = new HBox();
        botMenuActions.setAlignment(Pos.BOTTOM_CENTER);
        botMenuActions.setPadding(new Insets(20));
        botMenuActions.setMaxHeight(Region.USE_PREF_SIZE);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        botMenuActions.getChildren().addAll(btnSolar, spacer, btnPlanet);
        StackPane.setAlignment(botMenuActions, Pos.BOTTOM_CENTER);

        Pane labelOverlay = new Pane(hoverLabel);
        labelOverlay.setMouseTransparent(true);




        root.getChildren().addAll(starField, planetLayer, resourceContainer, menuDrawer, botMenuActions, labelOverlay);
    }
    /**
     * Helper to create a button with a custom PNG icon for the menu drawer.
     */
    /**
     * Helper to create a button with a larger custom PNG icon.
     */
    private Button createMenuButton(String imageName) {
        Button btn = new Button();
        btn.setPrefSize(45, 45);
        btn.getStyleClass().add("menu-icon-button");

        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(stream));

                iconView.setFitWidth(40);
                iconView.setFitHeight(40);
                iconView.setPreserveRatio(true);

                iconView.setSmooth(true);

                btn.setGraphic(iconView);
            }
        } catch (Exception e) {
            System.err.println("Could not load menu icon: " + imageName);
        }

        return btn;
    }

    @Override
    public void onNotificationSent(NotificationRecord record) {
        javafx.application.Platform.runLater(() -> {
            // Correctly pass the mainWindow instance
            NotificationPopup.show(mainWindow, record);
        });
    }

    /**
     * Helper to create compact resource tracker boxes matching the lo-fi prototype.
     */
    /**
     * Updated helper to use custom PNG icons for the resource tracker.
     */
    private HBox createResourceBox(String imageName, String amount, String color) {
        HBox box = new HBox(8); // Increased spacing slightly for icons
        box.setPadding(new Insets(3, 10, 3, 10));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(125);
        box.setMaxWidth(125);
        box.setStyle("-fx-background-color: " + color + "cc; -fx-background-radius: 15;");

        // Load custom PNG icon from resources
        javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                iconView.setImage(new javafx.scene.image.Image(stream));
                iconView.setFitWidth(18); // Scaled for the compact HUD
                iconView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load menu resource icon: " + imageName);
        }

        Label val = new Label(amount);
        val.getStyleClass().add("title-small");
        val.setStyle("-fx-text-fill: white;"); // Ensure visibility on colored backgrounds

        box.getChildren().addAll(iconView, val);
        return box;
    }
    @Override
    public void initialise() {
        uk.ac.soton.comp2300.App.getInstance().getNotificationLogic().setListener(this);
    }




    /**--------Formats resource values into string------*/
    private String format(List<ResourceStack> prices){
        int money = 0;
        int metal = 0;
        int wood = 0;
        int stone = 0;

        for(ResourceStack value : prices) {
            switch(value.getType()){
                case MONEY -> money = value.getAmount();
                case METAL -> metal = value.getAmount();
                case WOOD -> wood = value.getAmount();
                case STONE -> stone = value.getAmount();
            }

        }
        return String.format("Cost: 🔘 %d  🪵 %d  🟡 %d  🪨 %d", metal, wood, money, stone);
    }

}