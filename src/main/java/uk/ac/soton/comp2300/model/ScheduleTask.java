package uk.ac.soton.comp2300.model;

import java.time.Duration;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.time.LocalTime;

public class ScheduleTask {

    private String deviceName;
    private LocalTime time; // start time
    private Duration duration;
    private String description;
    private final BooleanProperty active = new SimpleBooleanProperty(true);

    // created a sub fn here to avoid destory origin ui, will be remove soon plz use the new one
    public ScheduleTask(String deviceName, LocalTime time, String description) {
        this(deviceName, time, Duration.ofHours(1), description);
    }

    public ScheduleTask(String deviceName, LocalTime time, Duration duration, String description) {
        this.deviceName = deviceName;
        this.time = time;
        this.duration = duration;
        this.description = description;
    }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public Duration getDuration() {return duration; }
    public void setDuration() {this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BooleanProperty activeProperty() { return active; }
    public boolean isActive() { return active.get(); }
    public void setActive(boolean value) { active.set(value); }
}