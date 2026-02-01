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

public class MenuScene extends BaseScene {

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
            Circle star = new Circle(
                    random.nextInt(mainWindow.getWidth()),
                    random.nextInt(mainWindow.getHeight()),
                    random.nextDouble() * 1.5,
                    Color.WHITE
            );
            starField.getChildren().add(star);
        }

        VBox resourceContainer = new VBox(8);
        resourceContainer.setPadding(new Insets(15));
        resourceContainer.setAlignment(Pos.TOP_LEFT);

        resourceContainer.getChildren().addAll(
                createResourceBox("🟡", "1,340,600", "#d4af37"),
                createResourceBox("🔘", "8,975", "#a0a0a0"),
                createResourceBox("🪵", "25,000", "#8b4513")
        );

        Circle planet = new Circle(200, Color.web("#4CAF50"));
        StackPane planetLayer = new StackPane(planet);
        planetLayer.setAlignment(Pos.BOTTOM_CENTER);
        planetLayer.setPadding(new Insets(0, 0, -60, 0));
        planetLayer.setMouseTransparent(true);

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

        Button btnReminders = new Button("🕒");
        Button btnAnalytics = new Button("📈");
        Button btnSchedule = new Button("📅");
        Button btnTasks = new Button("✅");
        Button btnSettings = new Button("⚙");
        Button btnHelp = new Button("❓");

        for (Button b : new Button[]{btnReminders, btnAnalytics, btnSchedule, btnTasks, btnSettings, btnHelp}) {
            b.setPrefSize(45, 45);
            b.getStyleClass().add("menu-icon-button");
        }

        menuToggle.setOnAction(e -> {
            boolean visible = !dropDownItems.isVisible();
            dropDownItems.setVisible(visible);
            dropDownItems.setManaged(visible);
        });

        menuDrawer.getChildren().addAll(menuToggle, dropDownItems);

        HBox bottomActions = new HBox();
        bottomActions.setAlignment(Pos.BOTTOM_CENTER);
        bottomActions.setPadding(new Insets(20));

        Button btnSolar = new Button("☀️");
        Button btnBuild = new Button("🏗️");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomActions.getChildren().addAll(btnSolar, spacer, btnBuild);

        root.getChildren().addAll(starField, planetLayer, resourceContainer, bottomActions, menuDrawer);
    }

    private HBox createResourceBox(String icon, String amount, String color) {
        HBox box = new HBox(5); // Reduced spacing between icon and text
        box.setPadding(new Insets(3, 10, 3, 10)); // Slimmer padding
        box.setAlignment(Pos.CENTER_LEFT);

        // Set a smaller fixed width to match the lo-fi image
        box.setMinWidth(120);
        box.setMaxWidth(120);

        box.setStyle("-fx-background-color: " + color + "cc; " +
                "-fx-background-radius: 15; " + // More subtle rounding
                "-fx-border-color: rgba(255,255,255,0.3); " +
                "-fx-border-radius: 15;");

        Label iconLabel = new Label(icon);
        Label valueLabel = new Label(amount);

        // Use a smaller font size for the compact boxes
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        box.getChildren().addAll(iconLabel, valueLabel);
        return box;
    }

    @Override
    public void initialise() { }
}