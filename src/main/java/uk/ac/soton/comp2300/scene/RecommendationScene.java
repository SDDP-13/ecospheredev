package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;
import javafx.scene.layout.Region;

public class RecommendationScene extends BaseScene {

    public RecommendationScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: #F6F3FB;");

        Button backBtn = new Button("←");
        backBtn.setPrefSize(44, 44);
        backBtn.getStyleClass().add("menu-icon-button");
        backBtn.setOnAction(e -> mainWindow.loadScene(new ScheduleScene(mainWindow)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(20));

        Label title = new Label("Recommendations");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800;");

        VBox deviceList = new VBox(15);
        deviceList.setAlignment(Pos.TOP_CENTER);
        deviceList.setPadding(new Insets(10));
        deviceList.setMaxWidth(380);

        String[] devices = {
                "Washing Machine",
                "Dish Washer",
                "Radiator",
                "Air Conditioner",
                "TV",
                "Garden Lights"
        };
        for (String device : devices) {
            deviceList.getChildren().add(createDeviceBox(device));
        }
        VBox content = new VBox(20, title, deviceList);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(100, 20, 20, 20));
        root.getChildren().addAll(content, backBtn);
    }

    private HBox createDeviceBox(String deviceName) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(15));
        box.setMaxWidth(350);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Region icon = new Region();
        icon.setPrefSize(40, 40);
        icon.setStyle("-fx-background-color: #DCD0FF; -fx-background-radius: 10;");

        VBox textBox = new VBox(5);

        Label name = new Label(deviceName);
        name.setStyle("-fx-font-size: 16pxl -fx-font-weight: bold;");

        Button recBtn = new Button("Get Recommendation");
        recBtn.setOnAction(e -> showRecommendationPopup(deviceName));
        textBox.getChildren().addAll(name, recBtn);
        box.getChildren().addAll(icon, textBox);
        return box;
    }

    private void showRecommendationPopup(String deviceName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recommendation");
        alert.setHeaderText(deviceName);
        alert.setContentText("...");
        alert.showAndWait();
    }

    @Override
    public void initialise() { }
}
