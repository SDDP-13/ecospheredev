package uk.ac.soton.comp2300.event;

import uk.ac.soton.comp2300.model.Notification;
import uk.ac.soton.comp2300.model.dataBaseInterface;


import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;



public class NotificationLogic {

    private dataBaseInterface databaseInterface;
    private NotificationListenerInterface listener;


    //Thread
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> thread;

    public NotificationLogic(dataBaseInterface databaseInterface, NotificationListenerInterface listener){
        this.databaseInterface = databaseInterface;
        this.listener = listener;
    }
    public void setListener(NotificationListenerInterface listener) {
        this.listener = listener;
    }

    public void start () {
        if (thread != null && !thread.isCancelled()) return;
        thread = scheduler.scheduleAtFixedRate(this::sendDueNotifications, 0, 3, TimeUnit.SECONDS);
    }

    public void stop() {
        if (thread != null)
            thread.cancel(true);
        thread = null;
    }

    public void shutdown(){
        stop();
        scheduler.shutdownNow();
    }

    void sendDueNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();

            for (Notification n : databaseInterface.getAllNotifications()) {
                if (n.getStatus() != Notification.state.PENDING) continue;
                if (n.getToSendTime().isAfter(now)) continue;

                n.markSent(now);
                databaseInterface.saveChanges(n);

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
    public void markNotificationComplete(UUID notificationID){
        Notification n = databaseInterface.findWithID(notificationID);
        if (n == null) return;

        n.setCompleted();
        databaseInterface.saveChanges(n);

        NotificationRecord record = new NotificationRecord(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getScheduled_Time(),
                n.getType()
        );
        listener.notificationUpdated(record);

    }

}
