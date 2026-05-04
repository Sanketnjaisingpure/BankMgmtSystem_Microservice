package com.bank.service;

import com.bank.ENUM.NotificationStatus;
import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import com.bank.model.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for notification management.
 *
 * <p>Defines contracts for:</p>
 * <ul>
 *   <li>Querying notifications by various filters (customer, type, source, status)</li>
 *   <li>Analytics (counts by status and source)</li>
 *   <li>Notification lifecycle operations (retry failed, fetch by date range)</li>
 * </ul>
 *
 * <p>Kafka listener methods are handled directly in the implementation class
 * since they are infrastructure concerns, not business contracts.</p>
 */
public interface NotificationService {

    // ── Query Methods ──

    /**
     * Retrieve all notifications for a specific customer.
     *
     * @param customerId the customer's unique identifier
     * @return list of notifications ordered by most recent first
     */
    List<Notification> getNotificationsByCustomerId(UUID customerId);

    /**
     * Retrieve all notifications of a specific type (e.g., ACCOUNT_CREATED, DEPOSIT).
     *
     * @param notificationType the notification type to filter by
     * @return list of matching notifications ordered by most recent first
     */
    List<Notification> getNotificationsByType(NotificationType notificationType);

    /**
     * Retrieve all notifications from a specific source service (e.g., ACCOUNT_SERVICE).
     *
     * @param sourceService the originating microservice
     * @return list of matching notifications ordered by most recent first
     */
    List<Notification> getNotificationsBySourceService(SourceService sourceService);

    /**
     * Retrieve all notifications with a specific delivery status.
     *
     * @param status the notification status to filter by (PENDING, SENT, FAILED, RETRYING)
     * @return list of matching notifications ordered by most recent first
     */
    List<Notification> getNotificationsByStatus(NotificationStatus status);

    /**
     * Retrieve notifications for a customer filtered by notification type.
     *
     * @param customerId       the customer's unique identifier
     * @param notificationType the notification type to filter by
     * @return list of matching notifications
     */
    List<Notification> getNotificationsByCustomerAndType(UUID customerId, NotificationType notificationType);

    /**
     * Retrieve notifications for a customer filtered by source service.
     *
     * @param customerId    the customer's unique identifier
     * @param sourceService the originating microservice
     * @return list of matching notifications
     */
    List<Notification> getNotificationsByCustomerAndSource(UUID customerId, SourceService sourceService);

    /**
     * Retrieve all notifications linked to a specific reference entity.
     *
     * @param referenceId the reference identifier (account number, loan ID, card ID)
     * @return list of matching notifications
     */
    List<Notification> getNotificationsByReferenceId(String referenceId);

    /**
     * Retrieve notifications created within a date range.
     *
     * @param start the start of the date range (inclusive)
     * @param end   the end of the date range (inclusive)
     * @return list of matching notifications
     */
    List<Notification> getNotificationsByDateRange(LocalDateTime start, LocalDateTime end);

    // ── Analytics Methods ──

    /**
     * Count notifications by delivery status.
     * Useful for monitoring dashboards.
     *
     * @param status the status to count
     * @return total count of notifications with the given status
     */
    long countByStatus(NotificationStatus status);

    /**
     * Count notifications by source service.
     * Useful for analytics and service health monitoring.
     *
     * @param sourceService the source service to count
     * @return total count of notifications from the given service
     */
    long countBySourceService(SourceService sourceService);
}
