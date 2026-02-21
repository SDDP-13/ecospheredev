package uk.ac.soton.comp2300.scene;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import uk.ac.soton.comp2300.model.ScheduleManager;
import uk.ac.soton.comp2300.model.ScheduleTask;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.time.LocalTime;

public class ScheduleScene extends BaseScene {

    private VBox scheduleList;
    private VBox emptyBox;

    public ScheduleScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: #F6F3FB;");

        Button backBtn = new Button("←");
        backBtn.setPrefSize(44, 44);
        backBtn.getStyleClass().add("menu-icon-button");
        backBtn.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(20));

        Button recBtn = new Button("💡");
        recBtn.setPrefSize(44, 44);
        recBtn.getStyleClass().add("menu-icon-button");
        recBtn.setOnAction(e -> mainWindow.loadScene(new RecommendationScene(mainWindow)));
        StackPane.setAlignment(recBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(recBtn, new Insets(20));

        Tooltip recommendTip = new Tooltip("Recommendations");
        recommendTip.setShowDelay(javafx.util.Duration.ZERO);
        recommendTip.setStyle("-fx-background-color: #6C4AB6; -fx-text-fill: white;");
        recBtn.setTooltip(recommendTip);

        Label title = new Label("Schedules");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 800;");
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(20, 0, 0, 0));

        Label emptyLabel = new Label("No schedules yet");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #5A5A5A;");

        emptyBox = new VBox(emptyLabel);
        emptyBox.setAlignment(Pos.CENTER_LEFT);
        emptyBox.setPadding(new Insets(20));
        emptyBox.setStyle("-fx-background-color: #EDE7F7; -fx-background-radius: 18;");

        scheduleList = new VBox(15);
        scheduleList.setPadding(new Insets(10));
        scheduleList.setMaxWidth(400);

        Button addBtn = new Button("+ Add Schedule");
        addBtn.setPrefWidth(260);
        addBtn.setPrefHeight(50);
        addBtn.setStyle("-fx-background-color: #DCD0FF; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> showSchedulePopup(null));

        VBox content = new VBox(20, emptyBox, scheduleList, addBtn);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(120, 20, 20, 20));

        root.getChildren().addAll(content, title, backBtn, recBtn);

        ScheduleManager.getTasks().addListener((ListChangeListener<ScheduleTask>) c -> refreshScheduleList());
        refreshScheduleList();
    }

    private void refreshScheduleList() {
        scheduleList.getChildren().clear();
        boolean hasTasks = !ScheduleManager.getTasks().isEmpty();
        emptyBox.setVisible(!hasTasks);
        emptyBox.setManaged(!hasTasks);

        for (ScheduleTask task : ScheduleManager.getTasks()) {
            scheduleList.getChildren().add(createTaskBox(task));
        }
    }

    private HBox createTaskBox(ScheduleTask task) {
        HBox taskBox = new HBox(20);
        taskBox.setAlignment(Pos.CENTER_LEFT);
        taskBox.setPadding(new Insets(15));
        taskBox.setMaxWidth(350);
        taskBox.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        VBox textBox = new VBox(5);

        Label device = new Label(task.getDeviceName());
        device.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label time = new Label(String.format("Set for: %02d:%02d",
                task.getTime().getHour(),
                task.getTime().getMinute()));

        Label description = new Label(task.getDescription());
        description.setStyle("-fx-text-fill: #777; -fx-font-size: 12px;");

        textBox.getChildren().addAll(device, time, description);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> showSchedulePopup(task));

        taskBox.getChildren().addAll(textBox, spacer, editBtn);
        return taskBox;
    }

    private void showSchedulePopup(ScheduleTask taskToEdit) {

        Stage popup = new Stage();
        popup.initOwner(mainWindow.getScene().getWindow());
        popup.setTitle(taskToEdit == null ? "Add Schedule" : "Edit Schedule");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #F6F3FB; -fx-background-radius: 18;");

        ComboBox<String> deviceBox = new ComboBox<>();
        deviceBox.getItems().addAll("Washing Machine", "Dishwasher", "Dryer",
                "Radiator", "Air Conditioner", "TV", "Garden Lights", "Other");
        deviceBox.setPrefWidth(220);
        deviceBox.setPromptText("Select device");

        TextField customNameField = new TextField();
        customNameField.setPromptText("Enter custom device name");
        customNameField.setPrefWidth(220);
        customNameField.setVisible(false);
        customNameField.setManaged(false);

        deviceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = "Other".equals(newVal);
            customNameField.setVisible(isOther);
            customNameField.setManaged(isOther);
        });

        TextField descField = new TextField();
        descField.setPromptText("Task description");
        descField.setPrefWidth(220);

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 12);
        Spinner<Integer> minSpinner = new Spinner<>(0, 59, 00);

        Label hourLabel = new Label("Hour: ");
        Label minLabel = new Label("Minute: ");

        hourLabel.setPrefWidth(90);
        minLabel.setPrefWidth(90);

        HBox hourRow = new HBox(10, hourLabel, hourSpinner);
        HBox minRow = new HBox(10, minLabel, minSpinner);

        hourRow.setAlignment(Pos.CENTER);
        minRow.setAlignment(Pos.CENTER);

        hourSpinner.setEditable(true);
        minSpinner.setEditable(true);

        hourSpinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) try { hourSpinner.increment(0); } catch (Exception ignored) {}
        });
        minSpinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) try { minSpinner.increment(0); } catch (Exception ignored) {}
        });

        if (taskToEdit != null) {
            String existingDevice = taskToEdit.getDeviceName();
            if (deviceBox.getItems().contains(existingDevice)) {
                deviceBox.setValue(existingDevice);
            } else {
                deviceBox.setValue("Other");
                customNameField.setText(existingDevice);
                customNameField.setVisible(true);
                customNameField.setManaged(true);
            }
            descField.setText(taskToEdit.getDescription());
            hourSpinner.getValueFactory().setValue(taskToEdit.getTime().getHour());
            minSpinner.getValueFactory().setValue(taskToEdit.getTime().getMinute());
        }

        Button saveBtn = new Button("Save");
        saveBtn.setPrefWidth(120);
        saveBtn.setStyle(
                "-fx-background-color: #DCD0FF;" +
                        "-fx-text-fill: #1F1F1F;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8 16;"
        );

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setStyle(saveBtn.getStyle());

        HBox buttonRow = new HBox(15, saveBtn, cancelBtn);
        buttonRow.setAlignment(Pos.CENTER);

        saveBtn.setOnAction(e -> {

            String device = deviceBox.getValue();
            if (device == null) {
                return;
            }

            if ("Other".equals(device)) {
                device = customNameField.getText().trim();
                if (device.isEmpty()) {
                    showError("Please enter a name for the custom device.");
                    return;
                }
            }

            String desc = descField.getText().trim();
            int hour;
            int minute;

            try {
                hour = Integer.parseInt(hourSpinner.getEditor().getText());
                minute = Integer.parseInt(minSpinner.getEditor().getText());

                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showError("Please enter a valid time (00–23 hours, 00–59 minutes).");
                return;
            }

            LocalTime time = LocalTime.of(hour, minute);

            boolean success;
            if (taskToEdit == null) {
                success = ScheduleManager.addTask(new ScheduleTask(device, time, desc));
            } else {
                success = ScheduleManager.updateTask(taskToEdit, device, time, desc);
            }

            if (success) {
                popup.close();
            } else {
                showError("This device is already scheduled at that time.");
            }
        });

        cancelBtn.setOnAction(e -> popup.close());

        layout.getChildren().addAll(deviceBox, customNameField, descField, hourRow, minRow, buttonRow);
        popup.setScene(new Scene(layout));
        popup.show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    @Override
    public void initialise() {}
}