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
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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
        heading.setStyle("-fx-font-weight: bold;");



        Label login = new Label("1. Login");
        login.setStyle("-fx-font-weight: bold;");
        Label section1Text = new Label("The Login Page allows users to securely access their account or create a new one.");
        section1Text.setWrapText(true);
        Image help_login1 = new Image(getClass().getResource("/images/Help_login1.png").toExternalForm());
        ImageView login1_view = new ImageView(help_login1);
        login1_view.setFitWidth(300);
        login1_view.setPreserveRatio(true);

        TextFlow loginTextFlow = new TextFlow();
        Text login1 = new Text(" • Enter your username and password to log in\n");
        Text login2 = new Text(" • ");
        Text login3 = new Text("New users");
        login3.setStyle("-fx-font-weight: bold;");
        Text login4 = new Text(" can select 'Create Account' to register\n");
        Text login5 = new Text(" • Usernames must be unique");
        loginTextFlow.getChildren().addAll(login1, login2, login3, login4, login5);
        Image help_login2 = new Image(getClass().getResource("/images/Help_login2.png").toExternalForm());
        ImageView login2_view = new ImageView(help_login2);
        login2_view.setFitWidth(300);
        login2_view.setPreserveRatio(true);



        helpContent.getChildren().addAll(
            heading,
            login,
            section1Text,
            login1_view,
            loginTextFlow,
            login2_view
        );

        scrollPane.setContent(helpContent);

        root.getChildren().addAll(scrollPane, btnBack);
    }

    @Override public void initialise() {}
}