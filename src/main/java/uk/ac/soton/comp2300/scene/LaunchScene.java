package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.game_logic.GameController;
import uk.ac.soton.comp2300.model.game_logic.Planet;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class LaunchScene extends BaseScene {

    private VBox planetList;
    private int spacePortCount;
    private GameController controller;
    private String chosenName = "";
    private List<Planet> generatedPlanets = new ArrayList<>();

    public LaunchScene(MainWindow mainWindow, int count) { super(mainWindow); this.spacePortCount = count; }

    @Override
    public void build() {

        var app = uk.ac.soton.comp2300.App.getInstance();
        controller = app.getInstance().getGameController();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-black");

        Pane starField = new Pane();
        starField.setStyle("-fx-background-color: black;");
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Circle star = new Circle(random.nextInt(mainWindow.getWidth()), random.nextInt(mainWindow.getHeight()), random.nextDouble() * 1.5, Color.WHITE);
            starField.getChildren().add(star);
        }

        Pane shipHull = new Pane();
        shipHull.setMouseTransparent(true);
        shipHull.setStyle("-fx-background-color: grey;");

        StackPane windowFrame = new StackPane();
        windowFrame.setAlignment(Pos.CENTER);
        windowFrame.setPadding(new Insets(30));

        StackPane glassWindow = new StackPane();
        glassWindow.setMaxSize(700, 900);
        glassWindow.setStyle("""
            -fx-background-color: transparent;
            -fx-background-radius: 20;
            -fx-border-color: darkgray;
            -fx-border-radius: 20;
            -fx-border-width: 20;
        """);
        glassWindow.getChildren().add(starField);

        Label title = new Label("Choose a planet");
        title.getStyleClass().add("title-xlarge");
        title.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label("Enter a name, then select a destination");
        subtitle.getStyleClass().add("title-medium");
        subtitle.setStyle("-fx-text-fill: white;");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter planet name...");
        nameInput.setMaxWidth(250);

        nameInput.textProperty().addListener((obs, oldV, newV) -> {
            chosenName = newV.trim();

            for (Node node : planetList.getChildren()) {
                if (node instanceof HBox card) {
                    for (Node child : card.getChildren()) {
                        if (child instanceof Button btn) {
                            updateButtonStyle(btn);
                        }
                    }
                }
            }
        });

        planetList = new VBox(15);
        planetList.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(planetList);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(500);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");


        generatePlanets();
        refreshPlanetList();

        VBox container = new VBox(20, title, subtitle, nameInput, scroll);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        glassWindow.getChildren().addAll(container);
        windowFrame.getChildren().add(glassWindow);

        root.getChildren().addAll(shipHull, windowFrame);
    }

    private void refreshPlanetList() {
        planetList.getChildren().clear();

        for (int i = 0; i < generatedPlanets.size(); i++) {
            planetList.getChildren().add(createPlanetCard(generatedPlanets.get(i), i + 1));
        }
    }

    private void generatePlanets() {
        generatedPlanets.clear();
        Random random = new Random();
        for (int i = 0; i < spacePortCount; i++) {
            Planet p = new Planet(UUID.randomUUID().toString());
            generatedPlanets.add(p);
        }
    }

    private HBox createPlanetCard(Planet planet, int index) {
        HBox card = new HBox(15);
        card.getStyleClass().add("card");
        card.setMaxWidth(300);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-border-color: transparent; -fx-border-width: 2; -fx-border-radius: 6;");

        Label numberLabel = new Label(index + ".");
        numberLabel.getStyleClass().add("title-large");
        numberLabel.setStyle("-fx-text-fill: black; -fx-font-size: 24px;");
        numberLabel.setMinWidth(40);
        numberLabel.setMaxWidth(40);
        numberLabel.setAlignment(Pos.CENTER_LEFT);

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        FlowPane multiplierRow = createMultiplierRow(planet);

        textContainer.getChildren().addAll(multiplierRow);

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

        Button selectBtn = new Button("SELECT");
        selectBtn.setMinWidth(80);
        selectBtn.setAlignment(Pos.CENTER);
        selectBtn.setContentDisplay(ContentDisplay.CENTER);

        updateButtonStyle(selectBtn);

        selectBtn.setOnAction(e -> {
            if (chosenName.isEmpty()) return;

            planet.setName(chosenName);
            controller.addPlanet(planet);
            mainWindow.loadScene(new SolarSystemScene(mainWindow));
        });

        HBox leftSide = new HBox(8, numberLabel, preview);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(leftSide, textContainer, selectBtn);

        return card;
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

    private void updateButtonStyle(Button btn) {
        boolean enabled = !chosenName.isEmpty();
        btn.setDisable(!enabled);

        if (enabled) {
            btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        } else {
            btn.setStyle("-fx-background-color: grey; -fx-text-fill: black;");
        }
    }


    @Override public void initialise() {}
}
