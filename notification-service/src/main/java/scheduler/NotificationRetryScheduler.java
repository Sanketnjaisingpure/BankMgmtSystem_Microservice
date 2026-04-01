package scheduler;

import com.bank.model.Notification;
import com.bank.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class NotificationRetryScheduler {

    @Autowired
    private NotificationRepository notificationRepository;

    @Async
    @Scheduled(fixedRate = 600000)
    public void retryFailedNotifications() {

        List<Notification> failedNotifications = notificationRepository.findByStatus("FAILED");

        for(Notification notification : failedNotifications) {
            notification.setStatus("SUCCESS");
            try{
                System.out.println("Retry notification: " + notification.getNotificationId());
                if (notification.getRetryCount() < 3) {
                    notification.setRetryCount(notification.getRetryCount() + 1);
                } else {
                    notification.setStatus("PERMANENT_FAILURE");
                }

                notificationRepository.save(notification);
            } catch (Exception e) {
                System.out.println("Retry failed again ");

            }
        }
    }

}
