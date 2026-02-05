package uk.ac.soton.comp2300.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {

    public enum Source {USER, SYSTEM, GAME, SCHEDULER}
    public enum Type { REMINDER, SUGGESTION, GAME_EVENT, GAME_TASK_COMPLETE, ENERGY_ALERT}
    public enum Status {PENDING, SENT, UNREAD, READ, DISMISSED, COMPLETED, TIMED_OUT}

    // Origin and Type of message
    private String id;
    private Source source;
    private Type type;

    //currentStatus
    private Status status;

    //Contents
    private String title;
    private String message;
    private LocalDateTime toSendTime;

    //Record keeping
    // Origin of Notification - e.g. Game Task
    private String refId;
    private LocalDateTime sentAt;
    private LocalDateTime dismissedAt;

    public Notification(Source source, Type type, String title, String message, LocalDateTime toSendTime, String refId ){

        this.id = UUID.randomUUID().toString();
        this.source = source;
        this.type = type;
        this.title = title;
        this.message = message;
        this.toSendTime = toSendTime;
        this.refId = refId;
        this.status = Status.PENDING;
    }

    public String getId() {return id;}
    public Source getSource() {return source;}
    public Type getType() {return type;}
    public String getTitle() {return title;}
    public String getMessage() {return message;}
    public LocalDateTime getToSendTime() {return toSendTime;}
    public String getRefId() {return refId;}

    public Status getStatus() {return status;}
    public LocalDateTime getSentAt() {return sentAt;}
    public LocalDateTime getDismissedAt() {return dismissedAt;}

    public void setStatus (Status status) {this.status = status;}

    public void markSent (LocalDateTime when){
        this.status = Status.SENT;
        this.sentAt = when;
    }

    public void markDismissed(LocalDateTime when){
        this.status = Status.DISMISSED;
        this.dismissedAt = when;
    }

    public void markRead(LocalDateTime when){
        this.status = Status.READ;
    }

    public void markCompleted(LocalDateTime when){
        this.status = Status.COMPLETED;
    }
}
