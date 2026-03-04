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
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.HashMap;
import java.util.Map;

public class DashboardScene extends BaseScene {

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
        toggleBar.setPadding(new Insets(10, 0, 20, 0));
        toggleBar.setStyle("-fx-background-color: #FBFAFF; -fx-background-radius: 10;");
        toggleBar.setMaxWidth(320);
        Label day7 = new Label("7 Days");
        day7.setStyle("-fx-font-weight: bold; -fx-text-fill: #311B92; -fx-cursor: hand;");
        Label week4 = new Label("4 Weeks");
        week4.setStyle("-fx-text-fill: #7986CB; -fx-cursor: hand;");
        Label week12 = new Label("12 Weeks");
        week12.setStyle("-fx-text-fill: #7986CB; -fx-cursor: hand;");
        toggleBar.getChildren().addAll(day7, week4, week12);

        // --- SECTION 2: TASKS CHART & APPLIANCE PROGRESS ---
        int dailyTasksDone = app.getCompletedScheduledTasks();
        long completedAppliances = app.getRepository().getAllNotifications().stream()
                .filter(n -> n.getStatus() == uk.ac.soton.comp2300.model.Notification.Status.TASK_COMPLETED)
                .count();

        int weeklyTarget = 35;
        double progressRatio = Math.min(1.0, (double) completedAppliances / weeklyTarget);

        VBox weeklyProgressCard = createChartContainer("Tasks completed (%)");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 20);

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

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Mon", 30));
        series.getData().add(new XYChart.Data<>("Tue", 55));
        series.getData().add(new XYChart.Data<>("Wed", 28));
        series.getData().add(new XYChart.Data<>("Thu", dailyTasksDone));
        series.getData().add(new XYChart.Data<>("Fri", 0));
        series.getData().add(new XYChart.Data<>("Sat", 0));
        series.getData().add(new XYChart.Data<>("Sun", 0));
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
        for (var note : app.getRepository().getAllNotifications()) {
            counts.put(note.getTitle(), counts.getOrDefault(note.getTitle(), 0) + 1);
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        counts.forEach((name, count) -> pieData.add(new PieChart.Data(name, count)));

        PieChart pieChart = new PieChart(pieData);
        pieChart.setPrefSize(250, 180);
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);

        FlowPane legend = new FlowPane(10, 10);
        legend.setAlignment(Pos.CENTER);
        String[] colors = {"#E64A19", "#FFA000", "#7B1FA2", "#388E3C", "#1976D2"};
        int i = 0;
        for (PieChart.Data data : pieData) {
            HBox item = new HBox(5);
            item.setAlignment(Pos.CENTER_LEFT);
            Circle circle = new Circle(5, Color.web(colors[i % colors.length]));
            Label lbl = new Label(data.getName());
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
        ecoImpactCard.getChildren().addAll(
                impactTitle,
                createImpactRow("Money Saved", String.format("£%.2f", app.getTotalMoneySaved()), "#43A047"),
                createImpactRow("Carbon Offset", String.format("%.2f kg", app.getTotalCo2Saved()), "#1B5E20")
        );

        // --- SECTION 5: RESOURCES DATA (AT BOTTOM) ---
        GridPane resourceGrid = new GridPane();
        resourceGrid.setHgap(15); resourceGrid.setVgap(15);
        resourceGrid.setAlignment(Pos.CENTER);
        resourceGrid.add(createResourceBox("Gold", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.MONEY), "🟡"), 0, 0);
        resourceGrid.add(createResourceBox("Metal", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.METAL), "🔘"), 1, 0);
        resourceGrid.add(createResourceBox("Wood", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.WOOD), "🪵"), 0, 1);
        resourceGrid.add(createResourceBox("Stone", state.getResourceAmount(uk.ac.soton.comp2300.model.Resource.STONE), "🪨"), 1, 1);

        // ASSEMBLY
        content.getChildren().addAll(title, xpBox, toggleBar, weeklyProgressCard, deviceChartCard, ecoImpactCard, resourceGrid);

        // Navigation
        Button backBtn = new Button("←");
        backBtn.setPrefSize(44,44);
        backBtn.getStyleClass().add("menu-icon-button");
        backBtn.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(20));

        root.getChildren().addAll(scrollPane, backBtn);
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