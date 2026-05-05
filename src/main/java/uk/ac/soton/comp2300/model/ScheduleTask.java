package uk.ac.soton.comp2300.model;

import java.time.Duration;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class ScheduleTask {

    private final String id;
    private String deviceName;
    private int hour;
    private int minute;
    private long durationMinutes;
    private String description;
    private transient BooleanProperty active = new SimpleBooleanProperty(true);
    private String lastCompletedDate;

    // created a sub fn here to avoid destory origin ui, will be remove soon plz use the new one
    public ScheduleTask(String deviceName, LocalTime time, String description) {
        this(deviceName, time, Duration.ofHours(24), description);
    }

    public ScheduleTask(String deviceName, LocalTime time, Duration duration, String description) {
        this.id = UUID.randomUUID().toString();
        this.deviceName = deviceName;
        this.hour = time.getHour();
        this.minute = time.getMinute();
        this.durationMinutes = duration.toMinutes();
        this.description = description;
    }

    public String getId() { return id; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public LocalTime getTime() { return LocalTime.of(hour, minute); }
    public void setTime(LocalTime time) {
        this.hour = time.getHour();
        this.minute = time.getMinute();
    }

    public Duration getDuration() {return Duration.ofMinutes(durationMinutes); }

    public void setDuration(Duration duration) {
        this.durationMinutes = duration.toMinutes();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BooleanProperty activeProperty() {
        if (active == null) {
            active = new SimpleBooleanProperty(true);
        }
        return active;
    }
    public boolean isActive() { return activeProperty().get(); }
    public void setActive(boolean value) { activeProperty().set(value); }

    public LocalDate getLastCompletedDate() {
        return lastCompletedDate == null ? null : LocalDate.parse(lastCompletedDate);
    }
    public void setLastCompletedDate(LocalDate date) {
        this.lastCompletedDate = (date == null) ? null : date.toString();
    }
}