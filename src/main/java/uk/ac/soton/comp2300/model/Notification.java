package uk.ac.soton.comp2300.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {

    public enum Source {USER, SYSTEM, GAME, SCHEDULER}
    public enum Type { REMINDER, GAME_EVENT, ENERGY_ALERT}
    public enum Status {PENDING, SENT, TASK_COMPLETED, TIMED_OUT}

    // Origin and Type of message
    private String id;
    private Source source;
    private Type type;

    //currentStatus
    private Status status;

    //Contents - Title, Do What, When
    private String title;
    private String message;
    private LocalDateTime scheduled_Time;

    //When to send the Notification
    private LocalDateTime sendAt;

    //Record keeping
    // Origin of Notification - e.g. Game Task
    private String refId;
    private LocalDateTime sentAt;
    private LocalDateTime dismissedAt;
    private LocalDateTime completedAt;

    public Notification(Source source, Type type, String title, String message,LocalDateTime scheduled_Time, LocalDateTime sendAt, String refId ){

        this.id = UUID.randomUUID().toString();
        this.source = source;
        this.type = type;
        this.title = title;
        this.message = message;
        this.scheduled_Time = scheduled_Time;
        this.sendAt = sendAt;
        this.refId = refId;
        this.status = Status.PENDING;
    }

    public String getId() {return id;}
    public Type getType() {return type;}
    public String getTitle() {return title;}
    public String getMessage() {return message;}
    public LocalDateTime getToSendTime() {return sendAt;}
    public LocalDateTime getScheduled_Time() {return scheduled_Time;};

    public Status getStatus() {return status;}


    public void setStatus (Status status) {this.status = status;}

    public void markSent (LocalDateTime when){
        this.status = Status.SENT;
        this.sentAt = when;
    }

    public void setCompleted(LocalDateTime when){
        this.status = Status.TASK_COMPLETED;
    }

    public void setScheduled_Time(LocalDateTime newTime){
        this.scheduled_Time = newTime;
    }
}
