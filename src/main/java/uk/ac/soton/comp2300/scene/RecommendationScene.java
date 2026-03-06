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
import uk.ac.soton.comp2300.recommendation_logic.RecommendationService;

public class RecommendationScene extends BaseScene {

    public RecommendationScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-light");

        Button backBtn = new Button("←");
        backBtn.setPrefSize(44, 44);
        backBtn.getStyleClass().add("menu-icon-button");
        backBtn.setOnAction(e -> mainWindow.loadScene(new ScheduleScene(mainWindow)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(20));

        Label title = new Label("Recommendations");

        title.getStyleClass().add("title-xlarge");

        Label content = new Label(
                "EcoSphere: Recommended Device Usage for Energy Savings\n\n" +
                "Overview: \n" +
                "Optimising when you use your home appliances can help reduce energy costs, lower your environmental impact, and support grid stability. \n" +
                "The recommendations below are based on typical energy tariffs and peak load times. \n" +
                "1. Washing Machine & Dryer \n" +
                "• Best Time to Use: 22:00 – 6:00 (off-peak hours) \n" +
                "  Tips: \n" +
                "   • Run full loads to maximise efficiency.\n" +
                "   • Use cold-water cycles when possible.\n" +
                "   • Avoid using the dryer during peak hours; consider air drying.\n" +
                "2. Dishwasher \n" +
                "• Best Time to Use: 21:00 – 7:00 \n" +
                "  Tips: \n" +
                "   • Run dishwasher on eco-mode if available.\n" +
                "   • Delay start function can be used to schedule overnight operation.\n" +
                "3. Heating & Cooling Systems \n" +
                "• Best Time to Use: \n" +
                "   • Heating: 5:00 - 8:00, 18:00 - 22:00 (pre-heat only) \n" +
                "   • Cooling: 11:00 - 18:00 (during peak sun, use blinds/ventilation first) \n" +
                "  Tips: \n" +
                "   • Use programmable thermostats.\n" +
                "   • Avoid heating/cooling empty rooms.\n"
        );
        content.setWrapText(true);
        content.getStyleClass().add("title-medium");

        content.setStyle("-fx-text-fill: black;");

        title.setStyle("-fx-text-fill: black;");



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
        VBox content_1 = new VBox(20, title, deviceList);
        content_1.setAlignment(Pos.TOP_CENTER);
        content_1.setPadding(new Insets(100, 20, 20, 20));
        root.getChildren().addAll(content_1, backBtn);
    }

    private HBox createDeviceBox(String deviceName) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(15));
        box.setMaxWidth(350);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        String imageName = deviceName.replace(" ", "") + ".png";

        if (deviceName.equalsIgnoreCase("Dryer") || deviceName.equalsIgnoreCase("Tumble Dryer")) {
            imageName = "WashingMachine.png";
        }

        javafx.scene.image.ImageView deviceIcon = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                deviceIcon.setImage(new javafx.scene.image.Image(stream));
                deviceIcon.setFitWidth(40);
                deviceIcon.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load recommendation icon: " + imageName);
        }

        VBox textBox = new VBox(5);

        Label name = new Label(deviceName);
        name.getStyleClass().addAll("label-medium", "font-weight-2");

        name.setStyle("-fx-text-fill: black;");

        Button recBtn = new Button("Get Recommendation");
        recBtn.getStyleClass().add("button-small"); // Assuming you have a small button style
        recBtn.setOnAction(e -> showRecommendationPopup(deviceName));

        textBox.getChildren().addAll(name, recBtn);
        box.getChildren().addAll(deviceIcon, textBox);
        return box;
    }

    private void showRecommendationPopup(String deviceName) {
        RecommendationService recommendationService = new RecommendationService();
        String message = recommendationService.getRecommendation(deviceName);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recommendation");
        alert.setHeaderText(deviceName);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialise() { }
}
