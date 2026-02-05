package uk.ac.soton.comp2300.model;

import java.util.List;

public interface NotificationRepository {
    List<Notification> getAllNotifications();
    void saveChanges(Notification notification);
}
