package uk.ac.soton.comp2300.event;

import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.NotificationRepository;


import java.time.LocalDateTime;
import java.util.concurrent.*;



public class NotificationLogic {

    private NotificationRepository repository;
    private NotificationListenerInterface listener;


    //Thread
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;

    public NotificationLogic(NotificationRepository repository, NotificationListenerInterface listener){
        this.repository = repository;
        this.listener = listener;
    }
    public void setListener(NotificationListenerInterface listener) {
        this.listener = listener;
    }

    public void start () {
        if (task != null && !task.isCancelled()) return;
        task = scheduler.scheduleAtFixedRate(this::sendDueNotifications, 0, 3, TimeUnit.SECONDS);
    }

    public void stop() {
        if (task != null)
            task.cancel(true);
        task = null;
    }

    public void shutdown(){
        stop();
        scheduler.shutdownNow();
    }

    void sendDueNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();

            for (Notification n : repository.getAllNotifications()) {
                if (n.getStatus() != Notification.Status.PENDING) continue;
                if (n.getToSendTime().isAfter(now)) continue;

                n.markSent(now);
                repository.saveChanges(n);

                NotificationRecord record = new NotificationRecord(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getScheduled_Time(),
                        n.getType()
                );
                listener.onNotificationSent(record);
            }
        } catch (Exception e) {
           System.out.println("Send Notification Error");
           e.printStackTrace();
        }

    }


}
