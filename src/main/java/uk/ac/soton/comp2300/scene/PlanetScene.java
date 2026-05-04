package uk.ac.soton.comp2300.scene;


import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.event.RefreshVisuals;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.ResourceStack;
import uk.ac.soton.comp2300.model.game_logic.BuildingData;
import uk.ac.soton.comp2300.model.game_logic.BuildingType;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.*;

public class PlanetScene extends BaseScene implements RefreshVisuals  {


    private PlanetView planetView;

    private PerspectiveCamera camera;

    private BuildingType selectedBuildingType;
    private VBox buildMenu;
    private boolean buildmenuOpen = false;
    private List <HBox> buildCards = new ArrayList<>();
    private Map<HBox, BuildingType> buildCardMap = new HashMap<>();
    private Label buildErrorLabel;
    private PauseTransition errorTimer;

    private Label currentGoldLabel;
    private Label currentMetalLabel;
    private Label currentWoodLabel;
    private Label currentStoneLabel;
    private int prevGold = -1;
    private int prevMetal = -1;
    private int prevWood = -1;
    private int prevStone = -1;

    private VBox buildingPopup;
    private Label popupTitle;
    private ImageView popupIcon;
    private VBox popupDescription;
    private Label popupTier;
    private Button actionBtn;

    private VBox researchPopup;
    private VBox researchPopupDescription;
    private Button researchBtn;

    private BuildingData selectedBuilding;

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

        selectedBuildingType = null;
        buildCards.clear();
        buildCardMap.clear();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: white;");

        Button btnBack = new Button("←");
        btnBack.setPrefSize(44, 44);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        buildingPopup = createBuildingPopup();
        StackPane.setAlignment(buildingPopup, Pos.TOP_LEFT);
        StackPane.setMargin(buildingPopup, new Insets(80, 0, 0, 20));

        researchPopup = createResearchPopup();
        StackPane.setAlignment(researchPopup, Pos.TOP_LEFT);
        StackPane.setMargin(researchPopup, new Insets(80, 0, 0, 20));

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
        planetView.setSelectionListener(this::showBuildingPopup);

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
            if (selectedBuildingType == null) {
                showBuildError("Select a building!");
                return;
            };
            if (!planetView.isCursorValid() ||
                    planetView.isDragging() ||
                    !buildmenuOpen) return;

            double theta = planetView.getCursorTheta();
            double phi = planetView.getCursorPhi();
            BuildingData newBuild = controller.placeBuilding(selectedBuildingType, theta, phi);
            if (newBuild != null) planetView.renderBuilding(newBuild);
            refreshBuildMenu();
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
        levelBar.setStyle("-fx-accent: #FFD54F;");
        levelBox.getChildren().addAll(levelLabel, levelBar);

// 2. Resource Section
        VBox resourceContainer = new VBox(8);
        resourceContainer.setAlignment(Pos.TOP_RIGHT); // Aligns the resource boxes to the right

         currentGoldLabel= new Label();
         currentMetalLabel = new Label();
         currentWoodLabel = new Label();
         currentStoneLabel = new Label();

        System.out.println("Making resource Container");
        resourceContainer.getChildren().addAll(

                createResourceBox("Coin.png",  currentGoldLabel, "#d4af37"),
                createResourceBox("Metal.png",  currentMetalLabel, "#a0a0a0"),
                createResourceBox("Wood.png", currentWoodLabel, "#8b4513"),
                createResourceBox("Stone.png",  currentStoneLabel, "#708090")
        );

        hudContainer.getChildren().addAll(levelBox, resourceContainer);
        StackPane.setAlignment(hudContainer, Pos.TOP_RIGHT);

        root.getChildren().addAll(starField, subScene, btnBack, btnBuild, hoverLabel, buildMenu, hudContainer, buildingPopup, researchPopup);

        refreshVisuals();


    }

    /** Helper method to create resource boxes */
    private HBox createResourceBox(String imageName, Label currentResLabel, String color) {
        HBox box = new HBox(8);
        box.setPadding(new Insets(6, 14, 6, 14));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(140);
        box.setMaxWidth(140);
        box.setStyle("-fx-background-color: " + color + "cc; -fx-background-radius: 15;");
        javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                iconView.setImage(new javafx.scene.image.Image(stream));
                iconView.setFitWidth(22);
                iconView.setPreserveRatio(true);
            }
        } catch (Exception e) {}
        currentResLabel.getStyleClass().add("resource-display");
        box.getChildren().addAll(iconView, currentResLabel);
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

        planetView.setBuildMode(buildmenuOpen);

        if (buildmenuOpen) {
            selectedBuildingType = null;
            selectedBuilding = null;
            buildingPopup.setVisible(false);
            buildingPopup.setManaged(false);
            researchPopup.setVisible(false);
            researchPopup.setManaged(false);
        }

        TranslateTransition move = new TranslateTransition(Duration.millis(250), buildMenu);
        move.setInterpolator(Interpolator.EASE_BOTH);
        if (buildmenuOpen) {
            move.setToY(0);
            refreshBuildMenu();
        } else {
            move.setToY(260);
        }

        move.play();
    }

    private VBox makeBuildMenu () {
        VBox menu = new VBox(8);
        menu.setPrefHeight(240);
        menu.setMinHeight(240);
        menu.setMaxHeight(240);
        menu.getStyleClass().add("card");

        Label title = new Label("Build Menu");
        title.getStyleClass().addAll("title-medium", "font-weight-2");

        buildErrorLabel = new Label();
        buildErrorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        buildErrorLabel.setVisible(false);
        buildErrorLabel.setManaged(false);
        buildErrorLabel.setMaxWidth(Double.MAX_VALUE);
        buildErrorLabel.setAlignment(Pos.CENTER);

        errorTimer = new PauseTransition(Duration.seconds(2));
        errorTimer.setOnFinished(e -> {
            buildErrorLabel.setVisible(false);
            buildErrorLabel.setManaged(false);
        });

        Button collapseBtn = new Button ("▾");
        collapseBtn.getStyleClass().add("menu-icon-button");
        collapseBtn.setPrefSize(28,28);
        collapseBtn.setOnAction(e->toggleBuildMenu());
        Region leftSpace = new Region();
        Region rightSpace = new Region();
        HBox.setHgrow(leftSpace, Priority.ALWAYS);
        HBox.setHgrow(rightSpace, Priority.ALWAYS);
        HBox header = new HBox (8, title, leftSpace, buildErrorLabel, rightSpace, collapseBtn);
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

        for (BuildingType type : BuildingType.values() ) {
            buildList.getChildren().add(buildEntry(type));
        }

        menu.getChildren().addAll(header, scrollPane);
        return menu;

    }

    private void refreshBuildMenu() {
        buildCards.clear();
        buildCardMap.clear();

        VBox buildList = (VBox) ((ScrollPane) buildMenu.getChildren().get(1)).getContent();
        buildList.getChildren().clear();

        for (BuildingType type : BuildingType.values()) {
            buildList.getChildren().add(buildEntry(type));
        }
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
        HBox resourceCost = formatWithIcons(App.getInstance().getGameController().getBuildingPrice(type));
        VBox textContent = new VBox(4, label, resourceCost);

        HBox buildCard = new HBox(15, buildingIcon, textContent);
        buildCard.setAlignment(Pos.CENTER_LEFT);
        buildCard.setPadding(new Insets(12));
        buildCard.getStyleClass().add("build-card");

        // Locked/Buildable check
        boolean buildable = App.getInstance().getGameController().buildable(type);
        if (!buildable) {
            buildCard.setOpacity(0.5);
            buildCard.setDisable(true);
        }

        buildCard.setOnMouseClicked(e -> {
            if (!App.getInstance().getGameController().buildable(type)) { return; }
            selectedBuildingType = type;
            System.out.println("Selected building type: " + selectedBuildingType);
        });

        buildCards.add(buildCard);
        buildCardMap.put(buildCard, type);
        return buildCard;
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

    private void showBuildError(String message) {
        buildErrorLabel.setText(message);
        buildErrorLabel.setVisible(true);
        buildErrorLabel.setManaged(true);

        errorTimer.stop();
        errorTimer.playFromStart();
    }



    private void textBounce(Label label) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), label);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.25);
        st.setToY(1.25);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private VBox createBuildingPopup() {

        popupTitle = new Label();
        popupTitle.getStyleClass().add("title-large");

        popupIcon = new ImageView();
        popupIcon.setFitWidth(38);
        popupIcon.setFitHeight(38);
        popupIcon.setPreserveRatio(true);

        popupTier = new Label();
        popupTier.getStyleClass().add("title-medium");
        popupTier.setStyle("-fx-font-weight: bold;");
        VBox.setMargin(popupTier, new Insets(0, 0, 2, 0));

        popupDescription = new VBox(6);
        popupDescription.setPadding(new Insets(6));
        popupDescription.setMaxWidth(260);
        popupDescription.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        VBox descriptionBox = new VBox(6, popupTier, popupDescription);
        descriptionBox.setPadding(new Insets(8));
        descriptionBox.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;"
                );
        descriptionBox.setMaxWidth(Double.MAX_VALUE);
        descriptionBox.setFillWidth(true);

        actionBtn = new Button();
        Button removeBtn = new Button("REMOVE");
        Button closeBtn = new Button("✕");

        closeBtn.setMaxWidth(32);
        actionBtn.setStyle("-fx-background-color: green; -fx-background-radius: 10; -fx-text-fill: white;");
        removeBtn.setStyle("-fx-background-color: red; -fx-background-radius: 10; -fx-text-fill: white;");

        closeBtn.setOnAction(e -> {
            buildingPopup.setVisible(false);
            planetView.clearSelectionExternally();
        });

        removeBtn.setOnAction(e -> {
            if (selectedBuilding == null) return;
            var controller = App.getInstance().getGameController();

            controller.removeBuilding(controller.getSelectedPlanet(), selectedBuilding);
            planetView.refreshBuildings();
            planetView.clearSelectionExternally();

            refreshBuildMenu();

            buildingPopup.setVisible(false);
            buildingPopup.setManaged(false);

            selectedBuilding = null;
        });


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, popupIcon, popupTitle, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(260);
        popupDescription.setMaxWidth(260);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox actions = new HBox(8, actionBtn, spacer2, removeBtn);
        actions.setMaxWidth(Double.MAX_VALUE);
        actions.setAlignment(Pos.CENTER);

        VBox box = new VBox(
                8,
                header,
                descriptionBox,
                actions
        );

        box.setPadding(new Insets(10));
        box.getStyleClass().add("card");
        box.setMaxWidth(260);
        box.setMinWidth(180);
        box.setMaxHeight(250);
        box.setFillWidth(true);

        box.setVisible(false);
        box.setManaged(false);
        box.setPickOnBounds(false);


        StackPane.setAlignment(box, Pos.TOP_LEFT);
        StackPane.setMargin(box, new Insets(80, 0, 0, 20));

        return box;
    }

    private void showBuildingPopup(BuildingData data) {
        var controller = App.getInstance().getGameController();

        researchPopup.setVisible(false);
        researchPopup.setManaged(false);

        selectedBuilding = data;

        popupTitle.setText(data.getType().name().replace("_", " "));

        String iconPath = "/images/" + data.getType().name().toLowerCase().replace("_", "") + ".png";

        try {
            popupIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            popupIcon.setImage(null);
        }

        popupDescription.getChildren().clear();
        Label desc = new Label(getDescription(data.getType()));
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #333; -fx-font-size: 14px;");

        if (data.getType().isUpgradeable()) {

            if (data.getLevel() >= controller.getMaxUpgradeLevel()) {
                popupTier.setText("TIER: MAX");
            } else {
                popupTier.setText("TIER: " + data.getLevel());
            }

            Label upgradeTitle = new Label("Upgrade Cost: ");
            upgradeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

            HBox costRow = formatWithIcons(data.getType().getUpgradeCost(data.getLevel()));

            boolean lockedByResearch = controller.isBlockedByResearch(data);
            Label researchWarning = new Label("Research the next tier to unlock upgrade");
            researchWarning.setWrapText(true);
            researchWarning.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            researchWarning.setVisible(lockedByResearch);
            researchWarning.setManaged(lockedByResearch);
            popupDescription.getChildren().addAll(desc, upgradeTitle, costRow, researchWarning);


        } else {
            popupTier.setText("TIER: SPECIAL");
            popupDescription.getChildren().addAll(desc);

        }

        String actionText;
        Runnable action;

        switch (data.getType()) {

            case RESEARCH_LAB -> {
                actionText = "RESEARCH";
                action = () -> {
                    showResearchPopup();
                    buildingPopup.setVisible(false);
                    buildingPopup.setManaged(false);
                };
            }

            case SPACEPORT -> {
                actionText = "LAUNCH";

                List<ResourceStack> launchCost = controller.getLaunchCost();

                Label costTitle = new Label("Launch Cost: ");
                costTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                HBox costRow = formatWithIcons(launchCost);

                popupDescription.getChildren().addAll(costTitle, costRow);

                action = () -> {
                    if (!controller.canLaunch()) return;

                    boolean success = controller.launch();

                    if (!success) return;

                    planetView.refreshBuildings();
                    refreshVisuals();

                    int count = controller.getNumberOfBuildingType(controller.getSelectedPlanet(), BuildingType.SPACEPORT);

                    PauseTransition delay = new PauseTransition(Duration.millis(250));
                    delay.setOnFinished(event -> {
                        mainWindow.loadScene(new LaunchScene(mainWindow, count));
                    });
                    delay.play();
                };
            }

            case MARKET -> {
                actionText = "TRADE";
                action = () -> {};
            }

            default -> {
                actionText = "UPGRADE";
                action = () -> {
                    if (controller.upgradeBuilding(data)) {
                        planetView.refreshBuildings();
                        refreshVisuals();
                        showBuildingPopup(data);
                    }
                };
            }
        }

        actionBtn.setText(actionText);
        actionBtn.setOnAction(e -> action.run());

        refreshVisuals();

        buildingPopup.setVisible(true);
        buildingPopup.setManaged(true);
    }

    private String getDescription(BuildingType type) {
        return switch (type) {
            case TOWN -> "Generates Money";
            case MINE -> "Generates Metal";
            case QUARRY -> "Generates Stone";
            case LUMBER_MILL -> "Generates Wood";
            case RESEARCH_LAB -> "Unlock new tiers of buildings and research upgrades";
            case SPACEPORT -> "Launch rockets and unlock new planets";
            case MARKET -> "Trade materials for other materials";
        };
    }

    private VBox createResearchPopup() {
        var controller = App.getInstance().getGameController();

        Label researchPopupTitle = new Label("Research Lab");
        researchPopupTitle.getStyleClass().add("title-large");

        ImageView researchPopupIcon = new ImageView();
        researchPopupIcon.setFitWidth(38);
        researchPopupIcon.setFitHeight(38);
        researchPopupIcon.setPreserveRatio(true);

        String iconPath = "/images/ResearchLab.png";
        try {
            researchPopupIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            researchPopupIcon.setImage(null);
        }

        researchPopupDescription = new VBox(6);
        researchPopupDescription.setPadding(new Insets(6));
        researchPopupDescription.setMaxWidth(260);
        researchPopupDescription.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Button closeBtn = new Button("✕");
        closeBtn.setMaxWidth(32);
        closeBtn.setOnAction(e -> {
            researchPopup.setVisible(false);
            planetView.clearSelectionExternally();
        });

        researchBtn = new Button("RESEARCH NEXT LEVEL");
        researchBtn.setStyle("-fx-background-color: green; -fx-background-radius: 10; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(researchPopupIcon, researchPopupTitle, spacer, closeBtn);
        VBox box = new VBox(
                8,
                header,
                researchPopupDescription,
                researchBtn
        );

        box.setPadding(new Insets(10));
        box.getStyleClass().add("card");
        box.setMaxWidth(260);
        box.setMinWidth(180);
        box.setMaxHeight(250);
        box.setFillWidth(true);
        box.setVisible(false);
        box.setManaged(false);

        return box;
    }

    private void showResearchPopup() {
        var controller = App.getInstance().getGameController();


        researchPopupDescription.getChildren().clear();

        Label desc = new Label("Your Research Level is capped at the number of labs you own on this planet. Maximum Tier: 5");
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");

        Label levelTier = new Label();
        int researchLevel = controller.getSelectedPlanet().getResearchLevel();
        if (researchLevel >= controller.getMaxUpgradeLevel()) {
            levelTier.setText("Research Level: MAX");
        } else {
            levelTier.setText("Research Level: " + researchLevel);
        }

        levelTier.getStyleClass().add("title-medium");
        levelTier.setStyle("-fx-font-weight: bold;");

        Label researchCostTitle = new Label("Research Cost: ");
        researchCostTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        HBox costRow = formatWithIcons(controller.getResearchCost());

        researchPopupDescription.getChildren().addAll(levelTier, desc, researchCostTitle, costRow);

        researchBtn.setOnAction(e -> {
            if (!controller.canResearchLevel()) return;
            boolean success = controller.increaseResearchLevel();

            if (!success) return;

            updateActionButton();
            refreshVisuals();
            showResearchPopup();
        });

        planetView.refreshBuildings();
        refreshVisuals();

        researchPopup.setVisible(true);
        researchPopup.setManaged(true);
    }

    private void updateActionButton() {
        if (selectedBuilding == null) return;

        var controller = App.getInstance().getGameController();

        switch (selectedBuilding.getType()) {
            case SPACEPORT -> {
                boolean canLaunch = controller.canLaunch();
                actionBtn.setDisable(!canLaunch);
                actionBtn.setOpacity(canLaunch ? 1.0 : 0.5);
            }
            case MARKET -> {
                actionBtn.setDisable(false);
                actionBtn.setOpacity(1.0);
            }

            case RESEARCH_LAB -> {
                if (researchPopup.isVisible()){
                    boolean canLevel = controller.canResearchLevel();
                    researchBtn.setDisable(!canLevel);
                    researchBtn.setOpacity(canLevel ? 1.0 : 0.5);
                } else {
                    actionBtn.setDisable(false);
                    actionBtn.setOpacity(1.0);
                }

            }
            default -> {
                boolean lockedByResearch = controller.isBlockedByResearch(selectedBuilding);
                boolean canUpgrade = controller.canUpgradeBuilding(selectedBuilding) && !lockedByResearch;
                actionBtn.setDisable(!canUpgrade);
                actionBtn.setOpacity(canUpgrade ? 1.0 : 0.5);
            }
        }
    }

    @Override
    public void refreshVisuals(){

        var controller = App.getInstance().getGameController();

        int gold = controller.getResourceAmount(Resource.MONEY);
        int metal = controller.getResourceAmount(Resource.METAL);
        int wood = controller.getResourceAmount(Resource.WOOD);
        int stone = controller.getResourceAmount(Resource.STONE);

        currentGoldLabel.setText(String.format("%,d", gold));
        currentMetalLabel.setText(String.format("%,d", metal));
        currentWoodLabel.setText(String.format("%,d", wood));
        currentStoneLabel.setText(String.format("%,d", stone));

        if (prevGold >= 0 && gold > prevGold) textBounce(currentGoldLabel);
        if (prevMetal >= 0 && metal > prevMetal) textBounce(currentMetalLabel);
        if (prevWood >= 0 && wood > prevWood) textBounce(currentWoodLabel);
        if (prevStone >= 0 && stone > prevStone) textBounce(currentStoneLabel);

        prevGold = gold;
        prevMetal = metal;
        prevWood = wood;
        prevStone = stone;

        updateActionButton();

        for (HBox card : buildCards) {
            BuildingType type = buildCardMap.get(card);
            boolean buildable = controller.buildable(type);

            card.getStyleClass().setAll("build-card");

            if (type == selectedBuildingType) {
                card.getStyleClass().add("build-card-selected");
            }

            if (buildable) {
                card.setOpacity(1.0);
                card.setDisable(false);
            } else {
                card.setOpacity(0.5);
                card.setDisable(true);

                if (type == selectedBuildingType) {
                    selectedBuildingType = null;
                }
            }
        }
    }

    @Override
    public void initialise() {
        root.setOnMousePressed(e -> planetView.onMousePressed(e));
        root.setOnMouseDragged(e -> planetView.onMouseDragged(e));
        root.addEventHandler(ScrollEvent.SCROLL, e -> planetView.onScroll(e, camera));
    }

}