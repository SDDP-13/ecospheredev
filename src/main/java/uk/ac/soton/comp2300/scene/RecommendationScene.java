package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class RecommendationScene extends BaseScene {

    public RecommendationScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-light");

        // 🔙 Back button
        Button btnBack = new Button("←");
        btnBack.setPrefSize(44, 44);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new ScheduleScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        // Title
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

        VBox textBox = new VBox(20, title, content);
        textBox.setAlignment(Pos.TOP_LEFT);
        textBox.setPadding(new Insets(120, 30, 30, 30));
        textBox.setMaxWidth(380);

        root.getChildren().addAll(textBox, btnBack);
    }

    @Override
    public void initialise() { }
}
