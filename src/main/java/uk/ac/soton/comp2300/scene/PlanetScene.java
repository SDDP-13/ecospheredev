package uk.ac.soton.comp2300.scene;


import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.ResourceStack;
import uk.ac.soton.comp2300.model.game_logic.BuildingData;
import uk.ac.soton.comp2300.model.game_logic.BuildingType;
import uk.ac.soton.comp2300.model.game_logic.GameController;
import uk.ac.soton.comp2300.model.game_logic.Planet;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.List;
import java.util.Random;

public class PlanetScene extends BaseScene {


    private PlanetView planetView;

    private PerspectiveCamera camera;

    private BuildingType selectedBuildingType = BuildingType.TOWN;
    private VBox buildMenu;
    private boolean buildmenuOpen = false;

    public PlanetScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {

        var app = uk.ac.soton.comp2300.App.getInstance();
        // Access the game state to get accurate resource counts
        var controller = app.getGameController();
        var state = controller.getGameState();
        var planetModel = state.getSelectedPlanet();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: white;");

        Button btnBack = new Button("←");
        btnBack.setPrefSize(44, 44);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        Label hoverLabel = new Label("");
        hoverLabel.setVisible(false);
        hoverLabel.setMouseTransparent(true);
        hoverLabel.getStyleClass().add("hover-description");
        hoverLabel.setText("Build Mode");
        StackPane.setAlignment(hoverLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(hoverLabel, new Insets(75, 10, 75, 10));

        Button btnBuild = new Button("🏗️");
        btnBuild.setPrefSize(45, 45);
        btnBuild.getStyleClass().add("menu-icon-button");
        btnBuild.setOnMouseEntered(e -> hoverLabel.setVisible(true));
        btnBuild.setOnMouseExited(e -> hoverLabel.setVisible(false));
        StackPane.setAlignment(btnBuild, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnBuild, new Insets(20));
        //btnBuild.setOnAction(e -> buildModeActive = !buildModeActive);
        btnBuild.setOnAction(e-> toggleBuildMenu());

        /** Build Menu */
        buildMenu = makeBuildMenu();
        buildMenu.setTranslateY(260);
        StackPane.setAlignment(buildMenu, Pos.BOTTOM_CENTER);
        StackPane.setMargin(buildMenu, new Insets(0,0,10,0));


        Pane starField = new Pane();
        starField.setStyle("-fx-background-color: black;");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(random.nextInt(mainWindow.getWidth()), random.nextInt(mainWindow.getHeight()), random.nextDouble() * 1.5, Color.WHITE);
            starField.getChildren().add(star);
        }


        planetView = new PlanetView(planetModel);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);
        camera.setTranslateZ(-600);

        SubScene subScene = new SubScene(
                planetView.getGroup(),
                mainWindow.getWidth(),
                mainWindow.getHeight(),
                true,
                null
        );
        subScene.setFill(Color.TRANSPARENT);
        subScene.setCamera(camera);


        subScene.setOnMouseEntered(e -> planetView.showBuildCursor());
        subScene.setOnMouseExited(e -> planetView.hideBuildCursor());

        subScene.setOnMouseMoved(e -> {
            if (!buildmenuOpen) {
                planetView.hideBuildCursor();
                return;
            }

            var pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode() == planetView.getSphere()) {
                Point3D coords = calculateSphericalCoords(pickResult.getIntersectedPoint());
                boolean isValid = controller.isBuildLocationFree(planetModel, coords.getX(), coords.getY());
                planetView.updateBuildCursor(coords.getX(), coords.getY(), isValid);
            }
        });

        subScene.setOnMouseClicked(e -> {
            if (selectedBuildingType == null) return;
            if (!planetView.isCursorValid() ||
                    planetView.isDragging() ||
                    !buildmenuOpen) return;

            double theta = planetView.getCursorTheta();
            double phi = planetView.getCursorPhi();
            BuildingData newBuild = controller.placeBuidling(planetModel, selectedBuildingType, theta, phi);
            if (newBuild != null) planetView.renderBuilding(newBuild);
        });

// --- HUD CONTAINER (LEVEL + RESOURCES) ---
        VBox hudContainer = new VBox(12);
        hudContainer.setPadding(new Insets(15));
        hudContainer.setAlignment(Pos.TOP_RIGHT); // Aligns levelBox and resourceContainer to the right side of the VBox
        hudContainer.setPickOnBounds(false); // Allows clicking through the HUD to rotate the planet

// 1. Level Data Section
        double[] levelData = app.getLevelData();
        int levelNum = (int) levelData[0];
        double levelProgress = levelData[3];

        VBox levelBox = new VBox(4);
        levelBox.setAlignment(Pos.TOP_RIGHT); // Aligns label and bar to the right within the levelBox
        Label levelLabel = new Label("Level " + levelNum);
        levelLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 0);");

        ProgressBar levelBar = new ProgressBar(levelProgress);
        levelBar.setPrefWidth(115);
        levelBar.setPrefHeight(10);
        levelBar.setStyle("-fx-accent: #FFD54F;"); // Golden color matching Dashboard
        levelBox.getChildren().addAll(levelLabel, levelBar);

// 2. Resource Section
        VBox resourceContainer = new VBox(8);
        resourceContainer.setAlignment(Pos.TOP_RIGHT); // Aligns the resource boxes to the right

        resourceContainer.getChildren().addAll(
                createResourceBox("Coin.png", String.format("%,d", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.MONEY)), "#d4af37"),
                createResourceBox("Metal.png", String.format("%,d", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.METAL)), "#a0a0a0"),
                createResourceBox("Wood.png", String.format("%,d", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.WOOD)), "#8b4513"),
                createResourceBox("Stone.png", String.format("%,d", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.STONE)), "#708090")
        );

        hudContainer.getChildren().addAll(levelBox, resourceContainer);

// IMPORTANT: Set the alignment of the hudContainer within the root StackPane
        StackPane.setAlignment(hudContainer, Pos.TOP_RIGHT);

// Assembly: Starfield (Background) -> SubScene (3D Planet) -> UI Buttons/Menus -> HUD (Top Layer)
        root.getChildren().addAll(starField, subScene, btnBack, btnBuild, hoverLabel, buildMenu, hudContainer);    }

    /** Helper method to create resource boxes */
    private HBox createResourceBox(String imageName, String amount, String color) {
        HBox box = new HBox(8);
        box.setPadding(new Insets(3, 10, 3, 10));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(115);
        box.setMaxWidth(115);
        box.setStyle("-fx-background-color: " + color + "cc; -fx-background-radius: 15;");

        javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                iconView.setImage(new javafx.scene.image.Image(stream));
                iconView.setFitWidth(18);
                iconView.setPreserveRatio(true);
            }
        } catch (Exception e) {}

        Label val = new Label(amount);
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");
        box.getChildren().addAll(iconView, val);
        return box;
    }


    public double snap(double value, double step) {
        return Math.round(value / step) * step;
    }

    private Point3D calculateSphericalCoords(Point3D intersectPoint) {
        Point3D planetCenter = planetView.getSphere().localToScene(0,0,0);
        Point3D local = intersectPoint.subtract(planetCenter);

        double x = local.getX();
        double y = local.getY();
        double z = local.getZ();
        double r = Math.sqrt(x * x + y * y + z * z);
        double theta = Math.acos(y / r);
        double phi = Math.atan2(z, x);

        double gridStep = Math.toRadians(15);
        theta = snap(theta, gridStep);
        phi = snap(phi, gridStep);

        return new Point3D(theta, phi, z);
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


        scrollPane.setPrefViewportHeight(140);

        buildList.getChildren().add(buildEntry(BuildingType.LUMBER_MILL));
        buildList.getChildren().add(buildEntry(BuildingType.QUARRY));
        buildList.getChildren().add(buildEntry(BuildingType.MINE));
        buildList.getChildren().add(buildEntry(BuildingType.TOWN));
        buildList.getChildren().add(buildEntry(BuildingType.MARKET));
        buildList.getChildren().add(buildEntry(BuildingType.SPACEPORT));
        buildList.getChildren().add(buildEntry(BuildingType.RESEARCH_LAB));

        /*
        buildList.getChildren().add(buildEntry("Lumber Mill", "Cost: 🔘 40  🪵 10  🟡 0"));
        buildList.getChildren().add(buildEntry("Quarry", "Cost: 🔘 80  🪵 30  🟡 200"));
        buildList.getChildren().add(buildEntry("Mine", "Cost: 🔘 20  🪵 200  🟡 10"));
        buildList.getChildren().add(buildEntry("Town", "Cost: 🔘 50  🪵 350  🟡 50"));
        buildList.getChildren().add(buildEntry("Market", "Cost: 🔘 200  🪵 30  🟡 500"));
        buildList.getChildren().add(buildEntry("Space Port", "Cost: 🔘 5500  🪵 1000  🟡 100"));
        buildList.getChildren().add(buildEntry("Research Lab", "Cost: 🔘 800  🪵 25  🟡 1000"));
        */
        menu.getChildren().addAll(header, scrollPane);
        return menu;

    }

    private HBox buildEntry(BuildingType type) {
        // 1. Convert Enum name (e.g., TOWN) to Filename (Town.png)
        String rawName = type.name().toLowerCase();
        String[] parts = rawName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        String buildingImgName = sb.toString() + ".png";

        Label label = new Label(sb.toString()); // Display name for the card
        label.getStyleClass().add("title-large-dark");

        // 2. Load the Icon
        javafx.scene.image.ImageView buildingIcon = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + buildingImgName);
            if (stream != null) {
                buildingIcon.setImage(new javafx.scene.image.Image(stream));
                buildingIcon.setFitWidth(50);
                buildingIcon.setPreserveRatio(true);
                buildingIcon.setSmooth(true);
            } else {
                System.err.println("FAILED to find image at: /images/" + buildingImgName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Assemble the card
        HBox resourceCost = formatWithIcons(type.getPrice());
        VBox textContent = new VBox(4, label, resourceCost);

        HBox resourceCard = new HBox(15, buildingIcon, textContent);
        resourceCard.setAlignment(Pos.CENTER_LEFT);
        resourceCard.setPadding(new Insets(12));
        resourceCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 3);");

        // Locked/Buildable check
        boolean buildable = App.getInstance().getGameController().buildable(type);
        if (!buildable) {
            resourceCard.setOpacity(0.5);
            resourceCard.setDisable(true);
        }

        return resourceCard;
    }


    /**
     * Renders resource costs using PNG icons instead of emojis
     */
    private HBox formatWithIcons(List<ResourceStack> prices) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        for (ResourceStack cost : prices) {
            if (cost.getAmount() > 0) {
                String iconFile = switch (cost.getType()) {
                    case MONEY -> "Coin.png";
                    case METAL -> "Metal.png";
                    case WOOD -> "Wood.png";
                    case STONE -> "Stone.png";
                    default -> "";
                };

                HBox item = new HBox(4);
                item.setAlignment(Pos.CENTER_LEFT);

                javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView();
                try {
                    var stream = getClass().getResourceAsStream("/images/" + iconFile);
                    if (stream != null) {
                        iconView.setImage(new javafx.scene.image.Image(stream));
                        iconView.setFitWidth(14);
                        iconView.setPreserveRatio(true);
                    }
                } catch (Exception e) {}

                Label val = new Label(String.valueOf(cost.getAmount()));
                val.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

                item.getChildren().addAll(iconView, val);
                container.getChildren().add(item);
            }
        }
        return container;
    }


    @Override
    public void initialise() {
        root.setOnMousePressed(e -> planetView.onMousePressed(e));
        root.setOnMouseDragged(e -> planetView.onMouseDragged(e));
        root.addEventHandler(ScrollEvent.SCROLL, e -> planetView.onScroll(e, camera));
    }

}