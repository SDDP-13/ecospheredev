package uk.ac.soton.comp2300.event;

import uk.ac.soton.comp2300.model.Notification.Type;

import java.time.LocalDateTime;

public record NotificationRecord(String id ,String title, String message,LocalDateTime scheduled_Time , Type type) {

}
