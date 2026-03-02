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
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: #F3EEF9;");

        // Get real data from the App session
        int totalTasksAvailable = app.getTasks().size(); // Total from JSON
        int tasksFinished = app.getCompletedScheduledTasks(); // Count of 'claimed' tasks

        // Calculate progress for the bar
        double progressPercentage = (totalTasksAvailable > 0)
                ? (double) tasksFinished / totalTasksAvailable
                : 0.0;

        String progressRatio = tasksFinished + "/" + totalTasksAvailable;

        VBox content = new VBox(20);
        content.setPadding(new Insets(80, 20, 20, 20));
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Pass the dynamic ratio to the card
        VBox todayProgress = createProgressCard("Today's Progress", "Daily Tasks", progressPercentage, progressRatio);

        // Resource Grid
        GridPane resourceGrid = new GridPane();
        resourceGrid.setHgap(15); resourceGrid.setVgap(15);
        resourceGrid.setAlignment(Pos.CENTER);
        resourceGrid.add(createResourceBox("Gold", app.getMoney(), "🟡"), 0, 0);
        resourceGrid.add(createResourceBox("Metal", app.getMetal(), "🔘"), 1, 0);
        resourceGrid.add(createResourceBox("Wood", app.getWood(), "🪵"), 0, 1);
        resourceGrid.add(createResourceBox("Total Xp", app.getTotalXp(), "⭐"), 1, 1);

        content.getChildren().addAll(title, todayProgress, resourceGrid);

        // Back Button
        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("menu-icon-button");
        backBtn.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(20));

        root.getChildren().addAll(content, backBtn);
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

        // This label now shows the dynamic count
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