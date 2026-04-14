package com.bank.service;

import com.bank.ENUM.ChannelType;
import com.bank.config.KafkaConstants;
import com.bank.event.AccountCreationEvent;
import com.bank.model.Notification;
import com.bank.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    @KafkaListener(topics = KafkaConstants.ACCOUNT_CREATION_TOPIC , groupId = KafkaConstants.ACCOUNT_CREATION_GROUP)
    public void sendAccountCreationNotification(AccountCreationEvent event , Acknowledgment ack) {

        logger.info("Sending account creation notification for customer: {}", event.getCustomerId());

        Notification notification = new Notification();
        notification.setCustomerId(event.getCustomerId());
        notification.setMessage(event.getMessage());
        if (event.getEmail() != null) {
            notification.setChannelType(ChannelType.EMAIL);
        } else {
            notification.setChannelType(ChannelType.SMS);
        }
        notification.setSentAt(LocalDateTime.now());
        try {
            notification.setStatus("SUCCESS");
            logger.info("Notification stored for customer {}", event.getCustomerId());
            notificationRepository.save(notification);

            ack.acknowledge();
        }
        catch (Exception e) {
            // TODO: handle exception
            // notification should never break logic
            notification.setStatus("FAILED");
            logger.error("Notification failed but account created", e);

        }
    }

    public void sendNotification(UUID senderCustomerId , String senderAccountNumber ,
                                        String receiverAccountNumber , BigDecimal amount){
        Notification notification = new Notification();
        notification.setCustomerId(senderCustomerId);
        notification.setSentAt(LocalDateTime.now());
        notification.setMessage("Transaction of amount " + amount + " from account " + senderAccountNumber + " to account " + receiverAccountNumber + " completed successfully");
    }

}
