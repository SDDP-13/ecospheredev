package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class DashboardScene extends BaseScene {

    public DashboardScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        var app = App.getInstance();
        var controller = app.getGameController();
        var state = controller.getGameState();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-light");

        // --- SCROLL LOGIC START ---
        // 1. Create a ScrollPane to handle overflow
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // 2. Main content container
        VBox content = new VBox(20);
        content.setPadding(new Insets(80, 20, 40, 20)); // Extra bottom padding for comfortable scrolling
        content.setAlignment(Pos.TOP_CENTER);

        // Add the content to the scrollPane
        scrollPane.setContent(content);
        // --- SCROLL LOGIC END ---

        Label title = new Label("Dashboard");
        title.getStyleClass().add("title-xlarge");

        // Task Progress Card
        int totalTasksAvailable = app.getTasks().size();
        int tasksFinished = app.getCompletedScheduledTasks();
        double progressPercentage = (totalTasksAvailable > 0) ? (double) tasksFinished / totalTasksAvailable : 0.0;
        VBox todayProgress = createProgressCard("Today's Progress", "Daily Tasks", progressPercentage, tasksFinished + "/" + totalTasksAvailable);

        // Infrastructure Summary
        var selectedPlanet = state.getSelectedPlanet();
        int buildingCount = (selectedPlanet != null) ? selectedPlanet.getBuildingData().size() : 0;
        VBox infraCard = new VBox(10);
        infraCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20;");
        Label infraTitle = new Label("Planet Infrastructure: " + (selectedPlanet != null ? selectedPlanet.getName() : "None"));
        infraTitle.setStyle("-fx-font-weight: 800; -fx-font-size: 16px; -fx-text-fill: #4527A0;");
        Label infraStats = new Label("Total Buildings Placed: " + buildingCount);
        infraStats.setStyle("-fx-text-fill: #666;");
        infraCard.getChildren().addAll(infraTitle, infraStats);

        // Eco & Cost Impact Card
        VBox impactCard = new VBox(15);
        impactCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20;");
        Label impactTitle = new Label("Eco & Cost Impact");
        impactTitle.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #2E7D32;");
        impactCard.getChildren().addAll(
                impactTitle,
                createImpactRow("Money Saved", String.format("£%.2f", app.getTotalMoneySaved()), "#43A047"),
                createImpactRow("Carbon Offset", String.format("%.2f kg", app.getTotalCo2Saved()), "#1B5E20")
        );

        // Resource Grid
        GridPane resourceGrid = new GridPane();
        resourceGrid.setHgap(15); resourceGrid.setVgap(15);
        resourceGrid.setAlignment(Pos.CENTER);
        resourceGrid.add(createResourceBox("Gold", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.MONEY), "🟡"), 0, 0);
        resourceGrid.add(createResourceBox("Metal", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.METAL), "🔘"), 1, 0);
        resourceGrid.add(createResourceBox("Wood", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.WOOD), "🪵"), 0, 1);
        resourceGrid.add(createResourceBox("Stone", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.STONE), "🪨"), 1, 1);

        VBox xpBox = createResourceBox("Total Xp", app.getTotalXp(), "⭐");
        xpBox.setPrefWidth(335);

        // Add everything to the content VBox
        content.getChildren().addAll(title, todayProgress, infraCard, impactCard, resourceGrid, xpBox);

        // Fixed Back Button (Outside the scroll area so it doesn't move)
        Button backBtn = new Button("←");
        backBtn.setPrefSize(44,44);
        backBtn.getStyleClass().add("menu-icon-button");
        backBtn.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(20));

        // Add the ScrollPane and the Fixed Back Button to the root StackPane
        root.getChildren().addAll(scrollPane, backBtn);
    }

    /**
     * Helper to create styled rows for the Impact Card.
     */
    private HBox createImpactRow(String label, String value, String color) {
        HBox row = new HBox();
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #777; -fx-font-size: 13px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label val = new Label(value);
        val.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-size: 15px;");
        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private VBox createProgressCard(String titleStr, String descStr, double progress, String ratioText) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20;");
        Label title = new Label(titleStr);
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #4A148C;");
        HBox labelRow = new HBox();
        Label desc = new Label(descStr);
        desc.setStyle("-fx-text-fill: #777; -fx-font-size: 13px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label ratio = new Label(ratioText);
        ratio.setStyle("-fx-text-fill: #555; -fx-font-weight: bold;");
        labelRow.getChildren().addAll(desc, spacer, ratio);
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(400);
        pb.setPrefHeight(12);
        pb.setStyle("-fx-accent: #B39DDB;");
        card.getChildren().addAll(title, labelRow, pb);
        return card;
    }

    private VBox createResourceBox(String name, int amount, String icon) {
        VBox box = new VBox(5);
        box.setPrefSize(160, 95);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        Label iconLbl = new Label(icon);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        Label valLbl = new Label(String.format("%,d", amount));
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4A148C;");
        box.getChildren().addAll(iconLbl, nameLbl, valLbl);
        return box;
    }

    @Override public void initialise() {}
}