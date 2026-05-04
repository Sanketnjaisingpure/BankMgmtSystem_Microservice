package com.bank.repository;

import com.bank.ENUM.NotificationStatus;
import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import com.bank.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Notification entity.
 * Provides query methods for filtering and analytics across all notification types.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /** Find all notifications for a specific customer, ordered by most recent first */
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    /** Find all notifications of a specific type (e.g., ACCOUNT_CREATED, LOAN_APPROVED) */
    List<Notification> findByNotificationTypeOrderByCreatedAtDesc(NotificationType notificationType);

    /** Find all notifications from a specific source service (e.g., ACCOUNT_SERVICE) */
    List<Notification> findBySourceServiceOrderByCreatedAtDesc(SourceService sourceService);

    /** Find all notifications with a specific delivery status (e.g., FAILED, PENDING) */
    List<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status);

    /** Find notifications for a customer filtered by notification type */
    List<Notification> findByCustomerIdAndNotificationTypeOrderByCreatedAtDesc(UUID customerId, NotificationType notificationType);

    /** Find notifications for a customer filtered by source service */
    List<Notification> findByCustomerIdAndSourceServiceOrderByCreatedAtDesc(UUID customerId, SourceService sourceService);

    /** Find notifications by reference ID (account number, loan ID, card ID) */
    List<Notification> findByReferenceIdOrderByCreatedAtDesc(String referenceId);

    /** Find notifications created within a date range */
    List<Notification> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    /** Count notifications by status — useful for monitoring dashboards */
    long countByStatus(NotificationStatus status);

    /** Count notifications by source service — useful for analytics */
    long countBySourceService(SourceService sourceService);
}
