package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp2300.event.NotificationListenerInterface;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;
import java.util.Random;
import uk.ac.soton.comp2300.event.NotificationRecord;
import uk.ac.soton.comp2300.ui.NotificationPopup;
/**
 * MenuScene: The central navigation hub for the EcoSphere application.
 * Focuses on Ebaa's Sprint 1 UI layout and navigation tasks.
 */
public class MenuScene extends BaseScene implements NotificationListenerInterface {

    public MenuScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());

        Pane starField = new Pane();
        starField.setStyle("-fx-background-color: black;");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(random.nextInt(mainWindow.getWidth()), random.nextInt(mainWindow.getHeight()), random.nextDouble() * 1.5, Color.WHITE);
            starField.getChildren().add(star);
        }

        Circle planet = new Circle(200, Color.web("#4CAF50"));
        StackPane planetLayer = new StackPane(planet);
        planetLayer.setAlignment(Pos.BOTTOM_CENTER);
        planetLayer.setPadding(new Insets(0, 0, -60, 0));
        planetLayer.setMaxWidth(Region.USE_PREF_SIZE);
        planetLayer.setMaxHeight(Region.USE_PREF_SIZE);
        planetLayer.setMouseTransparent(true);
        StackPane.setAlignment(planetLayer, Pos.BOTTOM_CENTER);

        var app = uk.ac.soton.comp2300.App.getInstance();

        VBox resourceContainer = new VBox(8);
        resourceContainer.setPadding(new Insets(15));
        resourceContainer.setAlignment(Pos.TOP_LEFT);
        resourceContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // We use String.format("%,d", ...) to ensure numbers like 1000000 look like 1,000,000
        resourceContainer.getChildren().addAll(
                createResourceBox("🟡", String.format("%,d", app.getMoney()), "#d4af37"),
                createResourceBox("🔘", String.format("%,d", app.getMetal()), "#a0a0a0"), // Must be getMetal()
                createResourceBox("🪵", String.format("%,d", app.getWood()), "#8b4513")   // Must be getWood()
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

        Button btnNotifications = new Button("🕒");
        Button btnDashboard = new Button("📈");
        Button btnSchedule = new Button("📅");
        Button btnTasks = new Button("✅");
        Button btnSettings = new Button("⚙");
        Button btnHelp = new Button("❓");
        Button btnSolar = new Button("☀️");
        Button btnBuild = new Button("🏗️");

        Button[] allButtons = {btnNotifications, btnDashboard, btnSchedule, btnTasks, btnSettings, btnHelp, btnSolar, btnBuild};
        String[] descriptions = {"Notifications", "Dashboard", "Schedules", "Tasks", "Settings", "Help", "Solar System", "Build Mode"};

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
                } else if (desc.equals("Build Mode")) {
                    hoverLabel.setLayoutX(bounds.getMinX() - 40);
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
        // Logic for the bottom bar buttons
        btnSolar.setOnAction(e -> mainWindow.loadScene(new SolarSystemScene(mainWindow)));
        btnBuild.setOnAction(e -> mainWindow.loadScene(new BuildScene(mainWindow)));
        menuToggle.setOnAction(e -> {
            boolean visible = !dropDownItems.isVisible();
            dropDownItems.setVisible(visible);
            dropDownItems.setManaged(visible);
        });

        dropDownItems.getChildren().addAll(btnNotifications, btnDashboard, btnSchedule, btnTasks, btnSettings, btnHelp);
        menuDrawer.getChildren().addAll(menuToggle, dropDownItems);

        HBox bottomActions = new HBox();
        bottomActions.setAlignment(Pos.BOTTOM_CENTER);
        bottomActions.setPadding(new Insets(20));
        bottomActions.setMaxHeight(Region.USE_PREF_SIZE);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomActions.getChildren().addAll(btnSolar, spacer, btnBuild);
        StackPane.setAlignment(bottomActions, Pos.BOTTOM_CENTER);

        Pane labelOverlay = new Pane(hoverLabel);
        labelOverlay.setMouseTransparent(true);

        root.getChildren().addAll(starField, planetLayer, resourceContainer, menuDrawer, bottomActions, labelOverlay);
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
    private HBox createResourceBox(String icon, String amount, String color) {
        HBox box = new HBox(5);
        box.setPadding(new Insets(3, 10, 3, 10));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(120);
        box.setMaxWidth(120);
        box.setStyle("-fx-background-color: " + color + "cc; -fx-background-radius: 15;");

        Label iconLabel = new Label(icon);
        Label val = new Label(amount);
        val.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        box.getChildren().addAll(iconLabel, val);
        return box;
    }

    @Override
    public void initialise() {
        uk.ac.soton.comp2300.App.getInstance().getNotificationLogic().setListener(this);
    }
}