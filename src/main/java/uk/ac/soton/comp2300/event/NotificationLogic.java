package uk.ac.soton.comp2300.event;

import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.NotificationRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.*;



public class NotificationLogic {

    private NotificationRepository repository;
    private NotificationListenerInterface listener;
    private Clock clock;


    //Thread
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;

    public NotificationLogic(NotificationRepository repository, NotificationListenerInterface listener, Clock clock){
        this.repository = repository;
        this.listener = listener;
        this.clock = clock;
    }

    public void start () {
        if (task != null && !task.isCancelled()) return;
        task = scheduler.scheduleAtFixedRate(this::runNotificationSafe, 0, 2, TimeUnit.SECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(true);
        task = null;
    }

    public void shutdown(){
        stop();
        scheduler.shutdownNow();
    }

    void sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now(clock);

        for (Notification n : repository.getAllNotifications()){
            if (n.getStatus() != Notification.Status.PENDING) continue;
            if (n.getToSendTime().isAfter(now)) continue;

            n.markSent(now);
            repository.saveChanges(n);

            NotificationRecord dto = new NotificationRecord(
                    n.getTitle(),
                    n.getMessage(),
                    n.getToSendTime(),
                    n.getType()
            );
            listener.onNotificationSent(dto);
        }

    }
    private void runNotificationSafe() {
        try {
            sendDueNotifications();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
