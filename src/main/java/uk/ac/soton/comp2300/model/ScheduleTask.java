package uk.ac.soton.comp2300.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.time.LocalTime;

public class ScheduleTask {

    private String deviceName;
    private LocalTime time;
    private String description;
    private final BooleanProperty active = new SimpleBooleanProperty(true);

    public ScheduleTask(String deviceName, LocalTime time, String description) {
        this.deviceName = deviceName;
        this.time = time;
        this.description = description;
    }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BooleanProperty activeProperty() { return active; }
    public boolean isActive() { return active.get(); }
    public void setActive(boolean value) { active.set(value); }
}