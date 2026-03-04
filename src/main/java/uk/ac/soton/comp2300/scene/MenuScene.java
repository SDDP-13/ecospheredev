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
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;
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
    private VBox buildMenu;
    private boolean buildmenuOpen = false;

    public MenuScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());

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
        planetButton.setOnAction((e) -> {mainWindow.loadScene(new PlanetScene(mainWindow, App.getInstance().getGameController().getGameState().getSelectedPlanet()));});
        StackPane planetLayer = new StackPane(planetButton);
        planetLayer.setAlignment(Pos.BOTTOM_CENTER);
        planetLayer.setPadding(new Insets(0, 0, -60, 0));
        planetLayer.setMaxWidth(Region.USE_PREF_SIZE);
        planetLayer.setMaxHeight(Region.USE_PREF_SIZE);
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

        /** Logic for the bottom menu buttons*/
        btnSolar.setOnAction(e -> mainWindow.loadScene(new SolarSystemScene(mainWindow)));
        btnBuild.setOnAction(e-> toggleBuildMenu());
        //btnBuild.setOnAction(e -> mainWindow.loadScene(new BuildScene(mainWindow)));

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
        botMenuActions.getChildren().addAll(btnSolar, spacer, btnBuild);
        StackPane.setAlignment(botMenuActions, Pos.BOTTOM_CENTER);

        Pane labelOverlay = new Pane(hoverLabel);
        labelOverlay.setMouseTransparent(true);

        /** Build Menu */
        buildMenu = makeBuildMenu();
        buildMenu.setTranslateY(260);
        StackPane.setAlignment(buildMenu, Pos.BOTTOM_CENTER);
        StackPane.setMargin(buildMenu, new Insets(0,0,10,0));


        root.getChildren().addAll(starField, planetLayer, resourceContainer, menuDrawer, botMenuActions, labelOverlay, buildMenu);
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
        val.getStyleClass().add("title-small");

        box.getChildren().addAll(iconLabel, val);
        return box;
    }

    @Override
    public void initialise() {
        uk.ac.soton.comp2300.App.getInstance().getNotificationLogic().setListener(this);
    }



    /** 3 Methods which handle build menu: toggleBuildMenu, makeBuildMenu, buildEntry */
    private void toggleBuildMenu() {
        buildmenuOpen = !buildmenuOpen;

        TranslateTransition move = new TranslateTransition(Duration.millis(250), buildMenu);
        move.setInterpolator(Interpolator.EASE_BOTH);
        if (buildmenuOpen) {
            move.setToY(0);
        } else {
            move.setToY(260);
        }

        move.play();

       // buildMenu.setVisible(buildmenuOpen);
       // buildMenu.setManaged(buildmenuOpen);

        //bottomActions.setVisible(!buildmenuOpen);
       // bottomActions.setManaged((!buildmenuOpen));
    }

    private VBox makeBuildMenu () {
        VBox menu = new VBox(8);
        menu.setPrefHeight(240);
        menu.setMinHeight(240);
        menu.setMaxHeight(240);
        menu.getStyleClass().add("card");

        Label title = new Label("Build Menu");
        title.getStyleClass().addAll("title-medium", "font-weight-2");

        Button collapseBtn = new Button ("▾");
        collapseBtn.getStyleClass().add("menu-icon-button");
        collapseBtn.setPrefSize(28,28);
        collapseBtn.setOnAction(e->toggleBuildMenu());

        Region headerSpace = new Region();
        HBox.setHgrow(headerSpace, Priority.ALWAYS);

        HBox header = new HBox (8, title, headerSpace, collapseBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0,0,4,0));



        VBox buildList = new VBox(10);
        buildList.getStyleClass().addAll("title-medium", "button-shape-rounded");

        ScrollPane scrollPane = new ScrollPane(buildList);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

// Optional: avoid scrollpane trying to size itself to content height
        scrollPane.setPrefViewportHeight(140);

        buildList.getChildren().add(buildEntry("Lumber Mill", "Cost: 🔘 40  🪵 10  🟡 0"));
        buildList.getChildren().add(buildEntry("Quarry", "Cost: 🔘 80  🪵 30  🟡 200"));
        buildList.getChildren().add(buildEntry("Mine", "Cost: 🔘 20  🪵 200  🟡 10"));
        buildList.getChildren().add(buildEntry("Town", "Cost: 🔘 50  🪵 350  🟡 50"));
        buildList.getChildren().add(buildEntry("Market", "Cost: 🔘 200  🪵 30  🟡 500"));
        buildList.getChildren().add(buildEntry("Space Port", "Cost: 🔘 5500  🪵 1000  🟡 100"));
        buildList.getChildren().add(buildEntry("Research Lab", "Cost: 🔘 800  🪵 25  🟡 1000"));

        menu.getChildren().addAll(header, scrollPane);
        return menu;

    }

    private HBox buildEntry(String name, String cost){
        Label label = new Label (name);
        label.getStyleClass().addAll("build-menu-title");
        Label resourceCost = new Label(cost);
        resourceCost.getStyleClass().add("build-menu-cost");

        VBox output = new VBox (4,label, resourceCost);

        Region image = new Region();
        image.setPrefSize(56, 56);
        image.getStyleClass().addAll("build-menu-image");

        HBox card = new HBox(12, image, output);
        card.getStyleClass().addAll("build-menu-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;

    }
}