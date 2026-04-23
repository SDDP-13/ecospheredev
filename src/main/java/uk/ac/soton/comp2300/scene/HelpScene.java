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
        heading.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        heading.setAlignment(Pos.CENTER);


        // LOGIN PAGE
        Label login = new Label("1. Login");
        login.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section1Text = new Label("The Login Page allows users to securely access their account or create a new one.");
        section1Text.setWrapText(true);
        /*Image help_login1 = new Image(getClass().getResource("/images/Help_login1.png").toExternalForm());
        ImageView login1_view = new ImageView(help_login1);
        login1_view.setFitWidth(300);
        login1_view.setPreserveRatio(true);*/

        TextFlow loginTextFlow = new TextFlow();
        Text login1 = new Text(" • Enter your username and password to log in\n");
        Text login2 = new Text(" • ");
        Text login3 = new Text("New users");
        login3.setStyle("-fx-font-weight: bold;");
        Text login4 = new Text(" can select 'Create Account' to register\n");
        Text login5 = new Text(" • Usernames must be unique");
        loginTextFlow.getChildren().addAll(login1, login2, login3, login4, login5);
        /*Image help_login2 = new Image(getClass().getResource("/images/Help_login2.png").toExternalForm());
        ImageView login2_view = new ImageView(help_login2);
        login2_view.setFitWidth(300);
        login2_view.setPreserveRatio(true);*/


        // HOME PAGE
        Label home = new Label("2. Home Page");
        home.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section2Text = new Label("The Home Page is the main hub of Ecosphere, providing an overview of your progress and access to all core features.");
        section2Text.setWrapText(true);
        TextFlow homeTextFlow = new TextFlow();
        Text home1 = new Text(" • ");
        Text home2 = new Text("Level and Inventory:");
        home2.setStyle("-fx-font-weight: bold;");
        Text home3 = new Text(" find this on the top left-hand corner of the Home Page\n");
        Text home4 = new Text("    • Your inventory includes Gold, Metal, Wood and Stone - which can be used to build structures in ");
        Text home5 = new Text("Build Mode\n");
        home5.setStyle("-fx-font-weight: bold;");
        Image help_home1 = new Image(getClass().getResource("/images/Help_home1.png").toExternalForm());
        ImageView home1_view = new ImageView(help_home1);
        home1_view.setFitWidth(170);
        home1_view.setPreserveRatio(true);
        VBox home1Container = new VBox(home1_view);
        home1Container.setAlignment(Pos.CENTER);
        Text home6 = new Text(" • ");
        Text home7 = new Text("Solar System View:");
        home7.setStyle("-fx-font-weight: bold;");
        Text home8 = new Text(" click the button on the bottom left-hand side to navigate to the solar system page\n");
        Text home9 = new Text(" • ");
        Text home10 = new Text("Planet View:");
        home10.setStyle("-fx-font-weight: bold;");
        Text home11 = new Text(" click the button on the bottom right-hand side to navigate to the Planet View, where you can view your planet's status and access Build Mode\n");
        Text home12 = new Text(" • ");
        Text home13 = new Text("Navigation Menu:");
        home13.setStyle("-fx-font-weight: bold;");
        Text home14 = new Text(" Click the button on the top right-hand side to access the drop-down menu\n ");
        Text home15 = new Text("     • The Core Features: Notifications, Dashboard, Schedules, Tasks, Settings and this Help Page");
        
        homeTextFlow.getChildren().addAll(home1,home2,home3,home4,home5,home6,home7,home8,home9,home10,home11,home12,home13,home14,home15);
        
        Image help_home2 = new Image(getClass().getResource("/images/Help_home2.png").toExternalForm());
        ImageView home2_view = new ImageView(help_home2);
        home2_view.setFitWidth(25);
        home2_view.setPreserveRatio(true);
        VBox home2Container = new VBox(home2_view);
        home2Container.setAlignment(Pos.CENTER);



        // SOLAR SYSTEM VIEW
        Label solarsystem = new Label("3. Solar System View");
        solarsystem.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section3Text = new Label("The Solar System View Page allows you to select the planet you want to manage.");
        section3Text.setWrapText(true);
        Label solar1 = new Label(" • Simply select the planet you want to manage and navigate back to the home page");
        solar1.setWrapText(true);
        Image help_solar1 = new Image(getClass().getResource("/images/Help_solar1.png").toExternalForm());
        ImageView solar1_view = new ImageView(help_solar1);
        solar1_view.setFitWidth(200);
        solar1_view.setPreserveRatio(true);
        VBox solar1Container = new VBox(solar1_view);
        solar1Container.setAlignment(Pos.CENTER);
        


        // PLANET VIEW
        Label planet = new Label("4. Planet View & Build Mode");
        planet.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section4Text = new Label("The Planet View allows you to interact directly with the planet you have selected, and build structures to generate resources.");
        section4Text.setWrapText(true);
        TextFlow planetTextFlow = new TextFlow();
        Text planet1 = new Text(" • ");
        Text planet2 = new Text("Build Mode:");
        planet2.setStyle("-fx-font-weight: bold;");
        Text planet3 = new Text(" to access build mode click the button on the bottom right-hand side\n");
        Image help_planet1 = new Image(getClass().getResource("/images/Help_planet1.png").toExternalForm());
        ImageView planet1_view = new ImageView(help_planet1);
        planet1_view.setFitWidth(200);
        planet1_view.setPreserveRatio(true);
        VBox planet1Container = new VBox(planet1_view);
        planet1Container.setAlignment(Pos.CENTER);
        Text planet4 = new Text("   • To place a structure, select the desired structure (must unlock in order to build) and place it anywhere on the planet\n");
        
        planetTextFlow.getChildren().addAll(planet1, planet2, planet3, planet4);

        Image help_planet2 = new Image(getClass().getResource("/images/Help_planet2.png").toExternalForm());
        ImageView planet2_view = new ImageView(help_planet2);
        planet2_view.setFitWidth(200);
        planet2_view.setPreserveRatio(true);
        VBox planet2Container = new VBox(planet2_view);
        planet2Container.setAlignment(Pos.CENTER);



        // NOTIFICATIONS
        Label notif = new Label("5. Notifications");
        notif.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section5Text = new Label("The Notifications page provides reminders and updates.");
        section5Text.setWrapText(true);
        TextFlow notifTextFlow = new TextFlow();
        Text notif1 = new Text(" • Find current notifications on this page\n");
        Text notif2 = new Text(" • ");
        Text notif3 = new Text(" Alerts:");
        notif3.setStyle("-fx-font-weight: bold;");
        Text notif4 = new Text(" notifications may appear as pop-up messages in-app or when the app is minimised, so you don't miss important updates\n");
        Image help_notif1 = new Image(getClass().getResource("/images/Help_notif1.png").toExternalForm());
        ImageView notif1_view = new ImageView(help_notif1);
        notif1_view.setFitWidth(250);
        notif1_view.setPreserveRatio(true);
        VBox notif1Container = new VBox(notif1_view);
        notif1Container.setAlignment(Pos.CENTER);
        Text notif5 = new Text("    • To dismiss, simply click the tick or cross button\n");
        Text notif6 = new Text(" • ");
        Text notif7 = new Text("'View All' Button:");
        notif7.setStyle("-fx-font-weight: bold;");
        Text notif8 = new Text(" takes you to the Notifications page\n");

        notifTextFlow.getChildren().addAll(notif1,notif2,notif3,notif4,notif5,notif6,notif7,notif8);

        // DASHBOARD
        Label dashboard = new Label("6. Dashboard");
        dashboard.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section6Text = new Label("The Dashboard provides an overview of your progress and activity within the application.");
        section6Text.setWrapText(true);
        TextFlow dashboardTextFlow = new TextFlow();
        Text dashboard1 = new Text("It includes details on: \n");
        Text dashboard2 = new Text("    • your current level and XP\n");
        dashboard2.setStyle("-fx-font-weight: bold;");
        Text dashboard3 = new Text("    • task activity\n");
        dashboard3.setStyle("-fx-font-weight: bold;");
        Image help_dashboard1 = new Image(getClass().getResource("/images/Help_dashboard1.png").toExternalForm());
        ImageView dashboard1_view = new ImageView(help_dashboard1);
        dashboard1_view.setFitWidth(250);
        dashboard1_view.setPreserveRatio(true);
        VBox dashboard1Container = new VBox(dashboard1_view);
        dashboard1Container.setAlignment(Pos.CENTER);
        Text dashboard4 = new Text("        • Can be viewed across different time periods (e.g. 7 days, 4 weeks)\n");
        Text dashboard5 = new Text("    • appliance usage\n");
        dashboard5.setStyle("-fx-font-weight: bold;");
        Text dashboard6 = new Text("    • eco & cost impact\n");
        dashboard6.setStyle("-fx-font-weight: bold;");
        //IMAGE
        Image help_dashboard2 = new Image(getClass().getResource("/images/Help_dashboard2.png").toExternalForm());
        ImageView dashboard2_view = new ImageView(help_dashboard2);
        dashboard2_view.setFitWidth(250);
        dashboard2_view.setPreserveRatio(true);
        VBox dashboard2Container = new VBox(dashboard2_view);
        dashboard2Container.setAlignment(Pos.CENTER);
        Text dashboard7 = new Text("        • energy saved in kWh\n");
        Text dashboard8 = new Text("        • money saved in GBP £\n");
        Text dashboard9 = new Text("        • carbon offset in kg\n");
        Text dashboard10 = new Text("       • includes 'Peak Savings Day' to highlight your most efficient day\n");
        Text dashboard11 = new Text("    • your resources and buildings inventory\n");
        dashboard11.setStyle("-fx-font-weight: bold;");

        dashboardTextFlow.getChildren().addAll(dashboard1,dashboard2,dashboard3,dashboard4,dashboard5,dashboard6,dashboard7,dashboard8,dashboard9,dashboard10,dashboard11);        
        



        // SCHEDULE
        Label schedule = new Label("7. Schedule");
        schedule.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section7Text = new Label("Use this page to schedule appliances, and to navigate to the Recommendations Page.");
        section7Text.setWrapText(true);
        TextFlow scheduleTextFlow = new TextFlow();
        Text schedule1 = new Text(" • To schedule appliances click the 'Add Schedule' button and fill in the information in the pop-up box\n");
        Text schedule2 = new Text(" • ");
        Text schedule3 = new Text("Recommendations Page:");
        schedule3.setStyle("-fx-font-weight: bold;");
        Text schedule4 = new Text(" scroll to find the appliance you want an energy recommendation for\n");
        scheduleTextFlow.getChildren().addAll(schedule1,schedule2,schedule3,schedule4);

        //TASKS
        Label tasks = new Label("8. Tasks");
        tasks.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section8Text = new Label("Find your daily tasks here. They reset daily at 08:00 GMT.");
        section8Text.setWrapText(true);

        TextFlow tasksTextFlow = new TextFlow();
        Text tasks1 = new Text(" • Each task includes a description, reward and task status\n");
        Text tasks2 = new Text(" • Tasks have three states: ");
        Text tasks3 = new Text("Locked, Claimable, Claimed\n");
        tasks3.setStyle("-fx-font-weight: bold;");
        Image help_tasks1 = new Image(getClass().getResource("/images/Help_task1.png").toExternalForm());
        ImageView tasks1_view = new ImageView(help_tasks1);
        tasks1_view.setFitWidth(300);
        tasks1_view.setPreserveRatio(true);
        VBox tasks1Container = new VBox(tasks1_view);
        tasks1Container.setAlignment(Pos.CENTER);
        Text tasks4 = new Text("      • To unlock tasks, complete the actions in the task description\n");
        Image help_tasks2 = new Image(getClass().getResource("/images/Help_task2.png").toExternalForm());
        ImageView tasks2_view = new ImageView(help_tasks2);
        tasks2_view.setFitWidth(300);
        tasks2_view.setPreserveRatio(true);
        VBox tasks2Container = new VBox(tasks2_view);
        tasks2Container.setAlignment(Pos.CENTER);
        Image help_tasks3 = new Image(getClass().getResource("/images/Help_task3.png").toExternalForm());
        ImageView tasks3_view = new ImageView(help_tasks3);
        tasks3_view.setFitWidth(200);
        tasks3_view.setPreserveRatio(true);
        VBox tasks3Container = new VBox(tasks3_view);
        tasks3Container.setAlignment(Pos.CENTER);
        //VBox.setMargin(tasks3_view, new Insets(10, 0, 10, 0));

        tasksTextFlow.getChildren().addAll(tasks1,tasks2,tasks3,tasks4);



        // SETTINGS
        Label settings = new Label("9. Settings");
        settings.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label section9Text = new Label("Set your preferences for notifications, visual mode, data usage terms, crash reporting, and account details.");
        section9Text.setWrapText(true);


        helpContent.getChildren().addAll(
            heading,

            login,
            section1Text,
            //login1_view,
            loginTextFlow,
            //login2_view,

            home,
            section2Text,
            homeTextFlow,
            home1_view,
            home2_view,

            solarsystem,
            section3Text,
            solar1,
            solar1_view,

            planet,
            section4Text,
            planetTextFlow,
            planet1_view,
            planet2_view,

            notif,
            section5Text,
            notifTextFlow,
            notif1_view,

            dashboard,
            section6Text,
            dashboardTextFlow,
            dashboard1_view,
            dashboard2_view,

            schedule,
            section7Text,
            scheduleTextFlow,

            tasks,
            section8Text,
            tasksTextFlow,
            tasks1_view,
            tasks2_view,
            tasks3_view,

            settings,
            section9Text
        );

        scrollPane.setContent(helpContent);

        root.getChildren().addAll(scrollPane, btnBack);
    }

    @Override public void initialise() {}
}