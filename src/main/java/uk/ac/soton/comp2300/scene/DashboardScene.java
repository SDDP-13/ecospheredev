package uk.ac.soton.comp2300.scene;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.event.RefreshVisuals;
import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardScene extends BaseScene implements RefreshVisuals {

    private Label currentGoldLabel;
    private Label currentMetalLabel;
    private Label currentWoodLabel;
    private Label currentStoneLabel;

    private enum ViewMode { DAYS_7, WEEKS_4, WEEKS_12 }
    private ViewMode currentMode = ViewMode.DAYS_7;

    private final String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private VBox chartContainer;

    public DashboardScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        var app = App.getInstance();
        var controller = app.getGameController();
        var state = controller.getGameState();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-light");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox content = new VBox(25);
        content.setPadding(new Insets(80, 20, 40, 20));
        content.setAlignment(Pos.TOP_CENTER);
        scrollPane.setContent(content);

        Label title = new Label("Dashboard");
        title.getStyleClass().add("title-xlarge");

        // --- SECTION 1: XP DATA & LEVEL BAR (NOW AT TOP) ---
        double[] levelData = app.getLevelData();
        int level = (int) levelData[0];
        int xpInCurrentLevel = (int) levelData[1];
        int xpRequiredForThisLevel = (int) levelData[2];
        double levelProgress = levelData[3];

        VBox xpBox = new VBox(10);
        xpBox.setPrefWidth(335);
        xpBox.setAlignment(Pos.CENTER);
        xpBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label xpIcon = new Label("⭐");
        Label xpTitle = new Label("Total XP: " + String.format("%,d", app.getTotalXp()));
        xpTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4A148C;");

        Label levelLabel = new Label("Level " + level);
        levelLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7B1FA2;");

        ProgressBar levelBar = new ProgressBar(levelProgress);
        levelBar.setPrefWidth(280);
        levelBar.setPrefHeight(15);
        levelBar.setStyle("-fx-accent: #FFD54F;"); // Golden color for level bar

        Label nextLevelInfo = new Label(xpInCurrentLevel + " / " + xpRequiredForThisLevel + " XP to next level");
        nextLevelInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

        xpBox.getChildren().addAll(xpIcon, xpTitle, levelLabel, levelBar, nextLevelInfo);

// --- TIME TOGGLE BAR ---
        HBox toggleBar = new HBox(30);
        toggleBar.setAlignment(Pos.CENTER);

        Label day7 = new Label("7 Days");
        Label week4 = new Label("4 Weeks");
        Label week12 = new Label("12 Weeks");

        toggleBar.getChildren().addAll(day7, week4, week12);


        day7.setStyle(currentMode == ViewMode.DAYS_7 ? "-fx-font-weight: bold; -fx-text-fill: #311B92; -fx-cursor: hand;" : "-fx-text-fill: #7986CB; -fx-cursor: hand;");
        week4.setStyle(currentMode == ViewMode.WEEKS_4 ? "-fx-font-weight: bold; -fx-text-fill: #311B92; -fx-cursor: hand;" : "-fx-text-fill: #7986CB; -fx-cursor: hand;");
        week12.setStyle(currentMode == ViewMode.WEEKS_12 ? "-fx-font-weight: bold; -fx-text-fill: #311B92; -fx-cursor: hand;" : "-fx-text-fill: #7986CB; -fx-cursor: hand;");

        day7.setOnMouseClicked(e -> {
            currentMode = ViewMode.DAYS_7;
            build();
            mainWindow.getScene().setRoot(root); // Forces UI Refresh
        });

        week4.setOnMouseClicked(e -> {
            currentMode = ViewMode.WEEKS_4;
            build();
            mainWindow.getScene().setRoot(root);
        });

        week12.setOnMouseClicked(e -> {
            currentMode = ViewMode.WEEKS_12;
            build();
            mainWindow.getScene().setRoot(root);
        });

// --- SECTION 2: TASKS CHART & APPLIANCE PROGRESS ---
        // 1. Calculate current week range (Monday to Sunday)
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate monday = now.with(java.time.DayOfWeek.MONDAY);
        java.time.LocalDate sunday = now.with(java.time.DayOfWeek.SUNDAY);
        String weekRange = String.format("%s - %s", monday, sunday);

        // 2. Weekly Progress Logic
        long completedAppliances = app.getRepository().getAllNotifications().stream()
                .filter(n -> n.getStatus() == uk.ac.soton.comp2300.model.Notification.Status.TASK_COMPLETED)
                .count();

        int weeklyTarget = 35;
        double progressRatio = Math.min(1.0, (double) completedAppliances / weeklyTarget);

        // Header displays the date range
        VBox weeklyProgressCard = createChartContainer("Daily Task Activity (" + weekRange + ")");

        CategoryAxis xAxis = new CategoryAxis();

        NumberAxis yAxis = new NumberAxis(0, 7, 1);
        // Adjust Y-Axis scale based on mode
        if (currentMode != ViewMode.DAYS_7) {
            yAxis.setUpperBound(40); // Increase for weekly totals
            yAxis.setTickUnit(5);
            yAxis.setAutoRanging(false); // Force it to use the new upper bound
        }
        yAxis.setLabel("Tasks Done");

        // Dark Purple Axis Styling
        String axisStyle = "-fx-tick-label-fill: #311B92; " +
                "-fx-axis-label-fill: #311B92; " +
                "-fx-tick-mark-stroke: #311B92; " +
                "-fx-axis-line-stroke: #311B92; " +
                "-fx-axis-line-stroke-width: 2px;";
        xAxis.setStyle(axisStyle);
        yAxis.setStyle(axisStyle);

        BarChart<String, Number> weeklyChart = new BarChart<>(xAxis, yAxis);
        weeklyChart.setLegendVisible(false);
        weeklyChart.setPrefSize(300, 180);
        weeklyChart.setHorizontalGridLinesVisible(true);
        weeklyChart.setVerticalGridLinesVisible(false);

        // 3. Populate Chart: Pulls specifically from Task Scene completions
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> taskData = app.getDailyTaskCompletionMap();

        if (currentMode == ViewMode.DAYS_7) {
            for (int j = 0; j < 7; j++) {
                java.time.LocalDate date = monday.plusDays(j);
                series.getData().add(new XYChart.Data<>(dayNames[j], taskData.getOrDefault(date.toString(), 0.0)));
            }
        } else {
            int weeksToLoad = (currentMode == ViewMode.WEEKS_4) ? 4 : 12;
            for (int w = weeksToLoad - 1; w >= 0; w--) {
                java.time.LocalDate weekStart = monday.minusWeeks(w);
                double weeklyTotal = 0;
                for (int d = 0; d < 7; d++) {
                    weeklyTotal += taskData.getOrDefault(weekStart.plusDays(d).toString(), 0.0);
                }
                // Labeling based on how many weeks ago the data was recorded
                String label = (w == 0) ? "This Week" : w + "w ago";
                series.getData().add(new XYChart.Data<>(label, weeklyTotal));
            }
        }
        weeklyChart.getData().add(series);

        VBox weeklyStats = new VBox(8);
        Label progressTitle = new Label("Weekly Progress");
        progressTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #311B92;");

        HBox progressRow = new HBox();
        Label progressLabel = new Label("Recommended schedules completed");
        progressLabel.setStyle("-fx-text-fill: #7986CB; -fx-font-size: 11px;");
        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
        Label pLabel = new Label(completedAppliances + "/" + weeklyTarget);
        pLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #311B92;");
        progressRow.getChildren().addAll(progressLabel, spacer1, pLabel);

        ProgressBar weeklyBar = new ProgressBar(progressRatio);
        weeklyBar.setPrefWidth(280);
        weeklyBar.setStyle("-fx-accent: #B39DDB;");
        weeklyStats.getChildren().addAll(progressTitle, progressRow, weeklyBar);
        weeklyProgressCard.getChildren().addAll(weeklyChart, weeklyStats);

        // --- SECTION 3: APPLIANCE PIE CHART ---
        VBox deviceChartCard = createChartContainer("Appliance Schedule Split");

        Map<String, Integer> counts = new HashMap<>();
        List<Notification> allNotes = app.getRepository().getAllNotifications();
        int totalSchedules = allNotes.size();

        for (var note : allNotes) {
            counts.put(note.getTitle(), counts.getOrDefault(note.getTitle(), 0) + 1);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

// 1. Chart Data
        counts.forEach((name, count) -> {
            double percentage = (totalSchedules > 0) ? ((double) count / totalSchedules) * 100 : 0;
            // The name of the Data object is what appears as the chart label
            PieChart.Data data = new PieChart.Data(String.format("%.1f%%", percentage), count);
            pieData.add(data);
        });

        PieChart pieChart = new PieChart(pieData);
        pieChart.setPrefSize(250, 180);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);

// 2. Legend Logic: Labels show the appliance name
        FlowPane legend = new FlowPane(10, 10);
        legend.setAlignment(Pos.CENTER);
        String[] colors = {"#E64A19", "#FFA000", "#7B1FA2", "#388E3C", "#1976D2"};

        String[] originalNames = counts.keySet().toArray(new String[0]);

        int i = 0;
        for (PieChart.Data data : pieData) {
            HBox item = new HBox(5);
            item.setAlignment(Pos.CENTER_LEFT);
            Circle circle = new Circle(5, Color.web(colors[i % colors.length]));
            
            Label lbl = new Label(originalNames[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

            item.getChildren().addAll(circle, lbl);
            legend.getChildren().add(item);
            i++;
        }

        deviceChartCard.getChildren().addAll(pieChart, legend);

        // --- SECTION 4: ECO IMPACT ---
        VBox ecoImpactCard = new VBox(15);
        ecoImpactCard.setMaxWidth(320);
        ecoImpactCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label impactTitle = new Label("Eco & Cost Impact");
        impactTitle.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #2E7D32;");

        Label rangeLabel = new Label("Weekly Range: " + weekRange);
        rangeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #777; -fx-padding: -10 0 0 0;");

        ecoImpactCard.getChildren().addAll(
                impactTitle,
                rangeLabel, // Added the date range here
                createImpactRow("Energy Saved", String.format("%.2f kWh", app.getTotalEnergySaved()), "#2E7D32"),
                createImpactRow("Money Saved", String.format("£%.2f", app.getTotalMoneySaved()), "#43A047"),
                createImpactRow("Carbon Offset", String.format("%.2f kg", app.getTotalCo2Saved()), "#1B5E20"),
                createImpactRow("Peak Savings Day", app.getDailySavingsMap().entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("No data"), "#E64A19")
        );
        // --- SECTION 5: RESOURCES GAINED ---
        VBox resourcesSection = new VBox(15);
        resourcesSection.setAlignment(Pos.CENTER);
        VBox.setMargin(resourcesSection, new Insets(30, 0, 0, 0));
        Label resHeader = new Label("Resources Gained:");
        resHeader.getStyleClass().add("title-medium");
        resHeader.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        GridPane resourceGrid = new GridPane();
        resourceGrid.setHgap(15); resourceGrid.setVgap(15);
        resourceGrid.setAlignment(Pos.CENTER);


        currentGoldLabel = new Label();
        currentMetalLabel = new Label();
        currentWoodLabel = new Label();
        currentStoneLabel = new Label();

        resourceGrid.add(createResourceBox("Gold", currentGoldLabel, "Coin.png"), 0, 0);
        resourceGrid.add(createResourceBox("Metal", currentMetalLabel, "Metal.png"), 1, 0);
        resourceGrid.add(createResourceBox("Wood", currentWoodLabel, "Wood.png"), 0, 1);
        resourceGrid.add(createResourceBox("Stone", currentStoneLabel, "Stone.png"), 1, 1);


        resourcesSection.getChildren().addAll(resHeader, resourceGrid);

        // --- SECTION 6: STRUCTURES BUILT ---
        VBox structuresSection = new VBox(15);
        structuresSection.setAlignment(Pos.CENTER);

        Label structHeader = new Label("Structures Built:");
        structHeader.getStyleClass().add("title-medium");
        structHeader.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");

        GridPane structGrid = new GridPane();
        structGrid.setHgap(15); structGrid.setVgap(15);
        structGrid.setAlignment(Pos.CENTER);


        var selectedPlanet = state.getSelectedPlanet();
        var buildings = (selectedPlanet != null) ? selectedPlanet.getBuildingData()
                : java.util.List.<uk.ac.soton.comp2300.model.game_logic.BuildingData>of();

        // Row 0
        structGrid.add(createResourceBox("Towns", (int)buildings.stream().filter(b -> b.getType() == uk.ac.soton.comp2300.model.game_logic.BuildingType.TOWN).count(), "Town.png"), 0, 0);
        structGrid.add(createResourceBox("Lumber Mills", (int)buildings.stream().filter(b -> b.getType() == uk.ac.soton.comp2300.model.game_logic.BuildingType.LUMBER_MILL).count(), "LumberMill.png"), 1, 0);

        // Row 1
        structGrid.add(createResourceBox("Quarries", (int)buildings.stream().filter(b -> b.getType() == uk.ac.soton.comp2300.model.game_logic.BuildingType.QUARRY).count(), "Quarry.png"), 0, 1);
        structGrid.add(createResourceBox("Mines", (int)buildings.stream().filter(b -> b.getType() == uk.ac.soton.comp2300.model.game_logic.BuildingType.MINE).count(), "Mine.png"), 1, 1);

        // Row 2
        structGrid.add(createResourceBox("Markets", (int)buildings.stream().filter(b -> b.getType() == uk.ac.soton.comp2300.model.game_logic.BuildingType.MARKET).count(), "Market.png"), 0, 2);
        structGrid.add(createResourceBox("Labs", (int)buildings.stream().filter(b -> b.getType() == uk.ac.soton.comp2300.model.game_logic.BuildingType.RESEARCH_LAB).count(), "ResearchLab.png"), 1, 2);

        // Row 3 (Standalone)
        structGrid.add(createResourceBox("Spaceports", (int)buildings.stream().filter(b -> b.getType() == uk.ac.soton.comp2300.model.game_logic.BuildingType.SPACEPORT).count(), "Spaceport.png"), 0, 3);

        structuresSection.getChildren().addAll(structHeader, structGrid);
        // ASSEMBLY
        content.getChildren().addAll(
                title,
                xpBox,
                toggleBar,
                weeklyProgressCard,
                deviceChartCard,
                ecoImpactCard,
                resourcesSection,
                structuresSection
        );
        // Navigation
        Button backBtn = new Button("←");
        backBtn.setPrefSize(44,44);
        backBtn.getStyleClass().add("menu-icon-button");
        backBtn.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(20));

        root.getChildren().addAll(scrollPane, backBtn);
        refreshVisuals();

        day7.setStyle(currentMode == ViewMode.DAYS_7 ? "-fx-font-weight: bold; -fx-text-fill: #311B92;" : "-fx-text-fill: #7986CB;");
        week4.setStyle(currentMode == ViewMode.WEEKS_4 ? "-fx-font-weight: bold; -fx-text-fill: #311B92;" : "-fx-text-fill: #7986CB;");
        week12.setStyle(currentMode == ViewMode.WEEKS_12 ? "-fx-font-weight: bold; -fx-text-fill: #311B92;" : "-fx-text-fill: #7986CB;");
    }
    /**
     * Helper to create a consistent container for charts.
     * Matches the styled boxes in the dashboard mockup.
     */
    private VBox createChartContainer(String titleText) {
        VBox card = new VBox(12);
        card.setMaxWidth(320);
        card.setStyle("-fx-background-color: #F8F7FF; -fx-background-radius: 20; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);");

        Label lbl = new Label(titleText);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #5C6BC0;");

        card.getChildren().add(lbl);
        return card;
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

    private VBox createResourceBox(String name, Label currencyLabel, String imageName) {
        VBox box = new VBox(5);
        box.setPrefSize(160, 95);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        javafx.scene.image.ImageView resourceIcon = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                resourceIcon.setImage(new javafx.scene.image.Image(stream));
                resourceIcon.setFitWidth(25);
                resourceIcon.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load resource icon: " + imageName);
        }

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

       // Label valLbl = new Label(String.format("%,d", amount));
        currencyLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4A148C;");

        box.getChildren().addAll(resourceIcon, nameLbl, currencyLabel);
        return box;
    }

    /** Overloaded so that buildings can call it.**/
    private VBox createResourceBox(String name, int amount, String imageName) {
        VBox box = new VBox(5);
        box.setPrefSize(160, 95);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        javafx.scene.image.ImageView resourceIcon = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                resourceIcon.setImage(new javafx.scene.image.Image(stream));
                resourceIcon.setFitWidth(25);
                resourceIcon.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load resource icon: " + imageName);
        }

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        Label valLbl = new Label(String.format("%,d", amount));
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4A148C;");

        box.getChildren().addAll(resourceIcon, nameLbl, valLbl);
        return box;
    }

    @Override public void initialise() {}

    @Override
    public void refreshVisuals(){


        var state = App.getInstance().getGameController().getGameState();

        currentGoldLabel.setText(String.format("%,d", state.getResourceAmount(Resource.MONEY)));
        currentMetalLabel.setText(String.format("%,d", state.getResourceAmount(Resource.METAL)));
        currentWoodLabel.setText(String.format("%,d", state.getResourceAmount(Resource.WOOD)));
        currentStoneLabel.setText(String.format("%,d", state.getResourceAmount(Resource.STONE)));


    }
}
