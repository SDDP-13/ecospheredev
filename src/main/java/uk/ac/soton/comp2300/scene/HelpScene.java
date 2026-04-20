package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class HelpScene extends BaseScene {
    public HelpScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-light");

        Button btnBack = new Button("⬅");
        btnBack.setPrefSize(50, 50);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(mainWindow.getWidth(), mainWindow.getHeight());

        VBox helpContent = new VBox(20);
        helpContent.setPadding(new Insets(40));
        helpContent.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("How to use Ecosphere");
        heading.getStyleClass().add("title-large");

        Label section1 = new Label("1. Login");
        section1.setStyle("-fx-font-weight: bold");
        Label section1Text = new Label("If you are having trouble logging in, ....");

        Label section2 = new Label("2. Scheduling");
        section2.setStyle("-fx-font-weight: bold");
        Label section2Text = new Label("To schedule an appliance, ....");

        Label faqLabel = new Label("FAQ:");
        faqLabel.setStyle("-fx-font-weight: bold");
        
        Image help_schedule = new Image(getClass().getResource("/images/EcosphereHelp_AddSchedule.png").toExternalForm());
        ImageView imageView = new ImageView(help_schedule);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);

        helpContent.getChildren().addAll(heading, section1, section2, faqLabel, imageView);

        scrollPane.setContent(helpContent);

        root.getChildren().addAll(scrollPane, btnBack);
    }

    @Override public void initialise() {}
}