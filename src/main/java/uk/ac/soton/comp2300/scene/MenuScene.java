package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;
import java.util.Random;

/**
 * MenuScene: The central navigation hub for the EcoSphere application.
 * Focuses on Ebaa's Sprint 1 UI layout and navigation tasks.
 */
public class MenuScene extends BaseScene {

    public MenuScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());

        // 1. Background Layer: Starry Night
        Pane starField = new Pane();
        starField.setStyle("-fx-background-color: black;");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(
                    random.nextInt(mainWindow.getWidth()),
                    random.nextInt(mainWindow.getHeight()),
                    random.nextDouble() * 1.5,
                    Color.WHITE
            );
            starField.getChildren().add(star);
        }

        // 2. Planet Layer: The central game world graphic
        Circle planet = new Circle(200, Color.web("#4CAF50"));
        StackPane planetLayer = new StackPane(planet);
        planetLayer.setAlignment(Pos.BOTTOM_CENTER);
        planetLayer.setPadding(new Insets(0, 0, -60, 0));
        planetLayer.setMouseTransparent(true);

        // 3. Resource Layer: Top Left trackers (Money, Metal, Wood)
        VBox resourceContainer = new VBox(8);
        resourceContainer.setPadding(new Insets(15));
        resourceContainer.setAlignment(Pos.TOP_LEFT);
        resourceContainer.getChildren().addAll(
                createResourceBox("🟡", "1,340,600", "#d4af37"),
                createResourceBox("🔘", "8,975", "#a0a0a0"),
                createResourceBox("🪵", "25,000", "#8b4513")
        );

        // 4. Hover Label: Dynamic text that appears beside hovered buttons
        Label hoverLabel = new Label("");
        hoverLabel.setVisible(false);
        hoverLabel.setMouseTransparent(true);
        hoverLabel.getStyleClass().add("hover-description");

        // 5. Navigation Drawer Layer: Top Right vertical menu
        VBox menuDrawer = new VBox(10);
        menuDrawer.setPadding(new Insets(20));
        menuDrawer.setMaxWidth(Region.USE_PREF_SIZE);
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

        // Define menu buttons and their corresponding hover text
// Initialize Buttons
        Button btnNotifications = new Button("🕒");
        Button btnDashboard = new Button("📈"); // Renamed from btnAnalytics
        Button btnSchedule = new Button("📅");
        Button btnTasks = new Button("✅");
        Button btnSettings = new Button("⚙");
        Button btnHelp = new Button("❓");

// Update arrays for the hover loop
        Button[] menuButtons = {btnNotifications, btnDashboard, btnSchedule, btnTasks, btnSettings, btnHelp};
        String[] descriptions = {"Notifications", "Dashboard", "Schedules", "Tasks", "Settings", "Help"};


        for (int i = 0; i < menuButtons.length; i++) {
            Button b = menuButtons[i];
            String desc = descriptions[i]; // This now uses "Notifications" for the first button

            b.setPrefSize(45, 45);
            b.getStyleClass().add("menu-icon-button");

            b.setOnMouseEntered(e -> {
                hoverLabel.setText(desc);
                var bounds = b.localToScene(b.getBoundsInLocal());
                hoverLabel.setLayoutX(bounds.getMinX() - 95);
                hoverLabel.setLayoutY(bounds.getMinY() + 10);
                hoverLabel.setVisible(true);
            });
            b.setOnMouseExited(e -> hoverLabel.setVisible(false));
        }

        // Navigation logic for Settings button
        btnDashboard.setOnAction(e -> mainWindow.loadScene(new DashboardScene(mainWindow)));
        btnNotifications.setOnAction(e -> mainWindow.loadScene(new NotificationScene(mainWindow)));
        btnDashboard.setOnAction(e -> mainWindow.loadScene(new DashboardScene(mainWindow)));
        btnSchedule.setOnAction(e -> mainWindow.loadScene(new ScheduleScene(mainWindow)));
        btnTasks.setOnAction(e -> mainWindow.loadScene(new TaskScene(mainWindow)));
        btnSettings.setOnAction(e -> mainWindow.loadScene(new SettingsScene(mainWindow)));
        btnHelp.setOnAction(e -> mainWindow.loadScene(new HelpScene(mainWindow)));

        menuToggle.setOnAction(e -> {
            boolean visible = !dropDownItems.isVisible();
            dropDownItems.setVisible(visible);
            dropDownItems.setManaged(visible);
        });

        dropDownItems.getChildren().addAll(menuButtons);
        menuDrawer.getChildren().addAll(menuToggle, dropDownItems);

        // 6. Bottom Bar: Main actions (Solar System and Build Mode)
        HBox bottomActions = new HBox();
        bottomActions.setAlignment(Pos.BOTTOM_CENTER);
        bottomActions.setPadding(new Insets(20));

        Button btnSolar = new Button("☀️");
        Button btnBuild = new Button("🏗️");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomActions.getChildren().addAll(btnSolar, spacer, btnBuild);

        // Overlay for the hover label to ensure absolute positioning
        Pane labelOverlay = new Pane(hoverLabel);
        labelOverlay.setMouseTransparent(true);

        // Final assembly of layers
        root.getChildren().addAll(starField, planetLayer, resourceContainer, bottomActions, menuDrawer, labelOverlay);
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
        // Required by BaseScene
    }
}