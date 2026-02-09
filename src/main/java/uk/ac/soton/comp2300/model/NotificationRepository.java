package uk.ac.soton.comp2300.model;

import java.util.List;

public interface NotificationRepository {
    List<Notification> getAllNotifications();
    void saveChanges(Notification notification);
    void deleteNotification (Notification notification);
    void clearNotifications ();
    void add (Notification notification);
}
