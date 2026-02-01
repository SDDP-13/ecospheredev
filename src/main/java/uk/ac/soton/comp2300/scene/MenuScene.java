package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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

        // 1. STAR FIELD BACKGROUND
        Pane starField = new Pane();
        starField.setStyle("-fx-background-color: black;");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(random.nextInt(mainWindow.getWidth()),
                    random.nextInt(mainWindow.getHeight()),
                    random.nextDouble() * 1.5, Color.WHITE);
            starField.getChildren().add(star);
        }

        // 2. THE PLANET (Centered at bottom)
        Circle planet = new Circle(200, Color.web("#4CAF50"));
        StackPane planetLayer = new StackPane(planet);
        planetLayer.setAlignment(Pos.BOTTOM_CENTER);
        planetLayer.setPadding(new Insets(0, 0, -60, 0));
        planetLayer.setMouseTransparent(true); // Clicks pass through to background

        // 3. RIGHT-SIDE DROPDOWN MENU
        // We use an AnchorPane or a BorderPane-like alignment to force it right
        VBox menuDrawer = new VBox(10);
        menuDrawer.setPadding(new Insets(20));
        menuDrawer.setMaxWidth(Region.USE_PREF_SIZE); // Prevents VBox from stretching across screen
        menuDrawer.setAlignment(Pos.TOP_CENTER); // Aligns buttons within the VBox

        // The Hamburger Button
        Button menuToggle = new Button("☰");
        menuToggle.setPrefSize(50, 50);
        menuToggle.getStyleClass().add("hamburger-button");

        // The Purple Drawer
        VBox dropDownItems = new VBox(12);
        dropDownItems.setAlignment(Pos.TOP_CENTER);
        dropDownItems.setPadding(new Insets(15, 10, 15, 10));
        dropDownItems.getStyleClass().add("menu-dropdown-bg");
        dropDownItems.setVisible(false);
        dropDownItems.setManaged(false);

        // Menu Buttons (Matching Subtasks)
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

        dropDownItems.getChildren().addAll(btnReminders, btnAnalytics, btnSchedule, btnTasks, btnSettings, btnHelp);

        // Toggle Action
        menuToggle.setOnAction(e -> {
            boolean isNowVisible = !dropDownItems.isVisible();
            dropDownItems.setVisible(isNowVisible);
            dropDownItems.setManaged(isNowVisible);
        });

        menuDrawer.getChildren().addAll(menuToggle, dropDownItems);

        // --- THE FIX: Align the drawer to the Top Right of the StackPane ---
        StackPane.setAlignment(menuDrawer, Pos.TOP_RIGHT);

        // 4. BOTTOM NAVIGATION
        HBox bottomActions = new HBox();
        bottomActions.setAlignment(Pos.BOTTOM_CENTER);
        bottomActions.setPadding(new Insets(20));

        Button btnSolar = new Button("☀️");
        Button btnBuild = new Button("🏗️");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomActions.getChildren().addAll(btnSolar, spacer, btnBuild);

        // Assemble layers
        root.getChildren().addAll(starField, planetLayer, bottomActions, menuDrawer);
    }

    @Override
    public void initialise() { }
}