package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.game_logic.GameController;
import uk.ac.soton.comp2300.model.game_logic.GameState;
import uk.ac.soton.comp2300.model.game_logic.Planet;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.Random;

public class SolarSystemScene extends BaseScene {

    private VBox planetList;
    private GameController controller;

    public SolarSystemScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {

        var app = uk.ac.soton.comp2300.App.getInstance();
        controller = app.getGameController();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-black");

        // Back button to return to the main menu
        Button btnBack = new Button("⬅");
        btnBack.setPrefSize(50, 50);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        // Main container
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        // Star Field
        Pane starField = new Pane();
        starField.setStyle("-fx-background-color: black;");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(random.nextInt(mainWindow.getWidth()), random.nextInt(mainWindow.getHeight()), random.nextDouble() * 1.5, Color.WHITE);
            starField.getChildren().add(star);
        }

        // Title
        Label title = new Label("Solar System");
        title.getStyleClass().add("title-xlarge");
        title.setStyle("-fx-text-fill: white;");

        // Description
        Label description = new Label("Select a planet to manage:");
        description.getStyleClass().add("title-medium");
        description.setStyle("-fx-text-fill: white;");

        // Planet List
        planetList = new VBox(15);
        planetList.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(planetList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        refreshPlanetList();

        container.getChildren().addAll(title, description, scrollPane);
        root.getChildren().addAll(starField, container, btnBack);
    }

    private void refreshPlanetList() {
        planetList.getChildren().clear();

        for (Planet planet : controller.getPlanets()) {
            planetList.getChildren().add(createPlanetCard(planet));
        }
    }

    private HBox createPlanetCard(Planet planet) {
        HBox card = new HBox(15);
        card.getStyleClass().add("card");
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-border-color: transparent; -fx-border-width: 2; -fx-border-radius: 6;");


        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Label name = new Label(planet.getName());
        name.getStyleClass().add("title-large-font");
        name.setStyle("-fx-font-size: 24px;");

        Label buildingCount = new Label("Buildings: " + planet.getBuildingData().size());
        buildingCount.getStyleClass().add("label-small");
        buildingCount.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label researchLevel = new Label("Research Level: " + planet.getResearchLevel());
        researchLevel.getStyleClass().add("label-small");
        researchLevel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        FlowPane multiplierRow = createMultiplierRow(planet);

        textContainer.getChildren().addAll(name, multiplierRow, buildingCount, researchLevel);

        Circle preview = new Circle(25);

        try {
            String fileName = planet.getTextureID().toLowerCase() + ".png";
            var stream = getClass().getResourceAsStream("/images/planet_textures/" + fileName);

            if (stream != null) {
                preview.setFill(new ImagePattern(new javafx.scene.image.Image(stream)));
            } else {
                preview.setFill(Color.GRAY);
            }
        } catch (Exception e) {
            preview.setFill(Color.GRAY);
        }

        Button selectBtn = new Button();
        selectBtn.setMinWidth(80);
        selectBtn.setAlignment(Pos.CENTER);
        selectBtn.setContentDisplay(ContentDisplay.CENTER);

        boolean isSelected = planet == controller.getSelectedPlanet();

        if (isSelected) {
            setBtnSelected(selectBtn);
            card.setStyle("-fx-border-color: gold; -fx-border-width: 2; -fx-border-radius: 6;");
        } else {
            setBtnSelectable(selectBtn);
        }

        selectBtn.setOnAction(e -> {
            controller.setSelectedPlanet(planet);
            refreshPlanetList();
        });

        card.getChildren().addAll(preview, textContainer, selectBtn);

        return card;
    }

    private void setBtnSelected(Button btn) {
        btn.setText("SELECTED");
        btn.setDisable(true);

        btn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #999; -fx-font-size: 13px; -fx-background-radius: 20;");
    }

    private void setBtnSelectable(Button btn) {
        btn.setText("SELECT");
        btn.setDisable(false);

        btn.getStyleClass().clear();
        btn.getStyleClass().add("button-claim");
    }

    private FlowPane createMultiplierRow(Planet planet) {
        FlowPane row = new FlowPane();
        row.setHgap(10);
        row.setVgap(5);
        row.setAlignment(Pos.CENTER_LEFT);

        var mults = planet.getProductionMultipliers();

        for (var entry : mults.entrySet()) {
            Resource resource = entry.getKey();
            double value = entry.getValue();

            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);

            String iconFile = switch (resource) {
                case MONEY -> "Coin.png";
                case METAL -> "Metal.png";
                case WOOD -> "Wood.png";
                case STONE -> "Stone.png";
            };

            ImageView icon = new ImageView();
            try {
                var stream = getClass().getResourceAsStream("/images/" + iconFile);
                if (stream != null) {
                    icon.setImage(new Image(stream));
                    icon.setFitWidth(14);
                    icon.setPreserveRatio(true);
                }
            } catch (Exception ignored) {}

            Label label = new Label(String.format("x%.2f", value));
            label.getStyleClass().add("label-small");
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            item.getChildren().addAll(icon, label);
            row.getChildren().add(item);

        }

        return row;
    }

    @Override public void initialise() {}
}