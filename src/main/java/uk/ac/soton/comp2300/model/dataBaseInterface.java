package uk.ac.soton.comp2300.model;

import java.util.List;
import java.util.UUID;

public interface dataBaseInterface {
    List<Notification> getAllNotifications();
    void saveChanges(Notification notification);
    void delete (Notification notification);
    void clearNotifications ();
    void add (Notification notification);
    Notification findWithID (UUID notificationID);
}

