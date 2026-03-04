package uk.ac.soton.comp2300.scene;


import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.game_logic.BuildingData;
import uk.ac.soton.comp2300.model.game_logic.BuildingType;
import uk.ac.soton.comp2300.model.game_logic.GameController;
import uk.ac.soton.comp2300.model.game_logic.Planet;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.Random;

public class PlanetScene extends BaseScene {

    private final Planet planetModel;
    private PlanetView planetView;

    private PerspectiveCamera camera;

    private boolean buildModeActive = false;
    private BuildingType selectedBuildingType = BuildingType.TOWN;

    public PlanetScene(MainWindow mainWindow, Planet planetModel) {
        super(mainWindow);
        this.planetModel = planetModel;
    }

    @Override
    public void build() {

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
        btnBuild.setOnAction(e -> buildModeActive = !buildModeActive);

        /*
        VBox bottomContainer = new VBox();
        bottomContainer.setMaxHeight(180);
        StackPane.setAlignment(bottomContainer, Pos.BOTTOM_CENTER);
        StackPane.setMargin(bottomContainer, new Insets(15));
        bottomContainer.setMaxWidth(Double.MAX_VALUE);
        bottomContainer.setStyle("-fx-background-color: rgba(92, 45, 145, 0.8); -fx-background-radius: 10;");


        ScrollPane buildPanel = new ScrollPane();
        buildPanel.setFitToHeight(true);
        buildPanel.setMaxWidth(Double.MAX_VALUE);
        buildPanel.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        buildPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        buildPanel.setVisible(true);
        buildPanel.setPannable(true);
        buildPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setMargin(buildPanel, new Insets(10));
        VBox.setVgrow(buildPanel, Priority.ALWAYS);
        buildPanel.addEventFilter(ScrollEvent.SCROLL, e -> {
           if (e.getDeltaY() != 0) {
               double deltaH = e.getDeltaY() / buildPanel.getContent().getBoundsInLocal().getWidth();
               buildPanel.setHvalue(buildPanel.getHvalue() - deltaH);
               e.consume();
           }
        });

        HBox content = new HBox(50);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        content.setPrefHeight(100);

        buildPanel.setContent(content);
        bottomContainer.getChildren().add(buildPanel);
        */

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
            if (!buildModeActive) {
                planetView.hideBuildCursor();
                return;
            }

            var pickResult = e.getPickResult();
            if (pickResult.getIntersectedNode() == planetView.getSphere()) {
                Point3D coords = calculateSphericalCoords(pickResult.getIntersectedPoint());
                GameController controller = App.getInstance().getGameController();
                boolean isValid = controller.isBuildLocationFree(planetModel, coords.getX(), coords.getY());
                planetView.updateBuildCursor(coords.getX(), coords.getY(), isValid);
            }
        });

        subScene.setOnMouseClicked(e -> {
            if (selectedBuildingType == null) return;
            if (!planetView.isCursorValid() ||
                    planetView.isDragging() ||
                    !buildModeActive) return;

            double theta = planetView.getCursorTheta();
            double phi = planetView.getCursorPhi();
            GameController controller = App.getInstance().getGameController();
            BuildingData newBuild = controller.placeBuidling(planetModel, selectedBuildingType, theta, phi);
            if (newBuild != null) planetView.renderBuilding(newBuild);
        });


        root.getChildren().addAll(starField, subScene, btnBack, btnBuild, hoverLabel);
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


    @Override
    public void initialise() {
        root.setOnMousePressed(e -> planetView.onMousePressed(e));
        root.setOnMouseDragged(e -> planetView.onMouseDragged(e));
        root.addEventHandler(ScrollEvent.SCROLL, e -> planetView.onScroll(e, camera));
    }

}
