package uk.ac.soton.comp2300.event;

import org.junit.jupiter.api.Test;
import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.NotificationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotificationSettingTest {

    @Test
    void disabledNotificationsAreNotSent() {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification(
                Notification.Source.SCHEDULER,
                Notification.Type.REMINDER,
                "Test",
                "Message",
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().minusMinutes(1),
                "test"
        ));

        NotificationRepository repository = new NotificationRepository() {
            @Override public List<Notification> getAllNotifications() { return notifications; }
            @Override public void saveChanges(Notification notification) { }
            @Override public void deleteNotification(Notification notification) { }
            @Override public void clearNotifications() { notifications.clear(); }
            @Override public void add(Notification notification) { notifications.add(notification); }
        };

        AtomicInteger sentCount = new AtomicInteger();
        Notification.setNotificationsEnabled(false);
        new NotificationLogic(repository, notification -> sentCount.incrementAndGet()).sendDueNotifications();

        assertEquals(0, sentCount.get());
        assertEquals(Notification.Status.PENDING, notifications.get(0).getStatus());
    }
}
