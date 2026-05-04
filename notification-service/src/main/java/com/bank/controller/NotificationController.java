package com.bank.controller;

import com.bank.ENUM.NotificationStatus;
import com.bank.ENUM.NotificationType;
import com.bank.ENUM.SourceService;
import com.bank.model.Notification;
import com.bank.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for notification management and analytics.
 *
 * <p>Provides endpoints to query notifications by various filters
 * (customer, type, source, status, reference, date range) and
 * analytics endpoints for monitoring dashboards.</p>
 *
 * <p>Base path: {@code /api/v1/notifications}</p>
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  QUERY ENDPOINTS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/notifications/customer/{customerId}
     * Retrieve all notifications for a specific customer.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Notification>> getByCustomerId(@PathVariable UUID customerId) {
        logger.info("GET /customer/{} - Fetching notifications for customer", customerId);
        List<Notification> notifications = notificationService.getNotificationsByCustomerId(customerId);
        logger.info("Returning {} notifications for customerId={}", notifications.size(), customerId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/type/{notificationType}
     * Retrieve all notifications of a specific type (e.g., ACCOUNT_CREATED, DEPOSIT, LOAN_APPROVED).
     */
    @GetMapping("/type/{notificationType}")
    public ResponseEntity<List<Notification>> getByType(@PathVariable NotificationType notificationType) {
        logger.info("GET /type/{} - Fetching notifications by type", notificationType);
        List<Notification> notifications = notificationService.getNotificationsByType(notificationType);
        logger.info("Returning {} notifications for type={}", notifications.size(), notificationType);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/source/{sourceService}
     * Retrieve all notifications from a specific source service (e.g., ACCOUNT_SERVICE, LOAN_SERVICE).
     */
    @GetMapping("/source/{sourceService}")
    public ResponseEntity<List<Notification>> getBySourceService(@PathVariable SourceService sourceService) {
        logger.info("GET /source/{} - Fetching notifications by source", sourceService);
        List<Notification> notifications = notificationService.getNotificationsBySourceService(sourceService);
        logger.info("Returning {} notifications from source={}", notifications.size(), sourceService);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/status/{status}
     * Retrieve all notifications with a specific delivery status (e.g., FAILED, PENDING, SENT).
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getByStatus(@PathVariable NotificationStatus status) {
        logger.info("GET /status/{} - Fetching notifications by status", status);
        List<Notification> notifications = notificationService.getNotificationsByStatus(status);
        logger.info("Returning {} notifications with status={}", notifications.size(), status);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/customer/{customerId}/type/{notificationType}
     * Retrieve notifications for a customer filtered by notification type.
     */
    @GetMapping("/customer/{customerId}/type/{notificationType}")
    public ResponseEntity<List<Notification>> getByCustomerAndType(
            @PathVariable UUID customerId,
            @PathVariable NotificationType notificationType) {

        logger.info("GET /customer/{}/type/{} - Fetching filtered notifications", customerId, notificationType);
        List<Notification> notifications = notificationService
                .getNotificationsByCustomerAndType(customerId, notificationType);
        logger.info("Returning {} notifications for customerId={} with type={}",
                notifications.size(), customerId, notificationType);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/customer/{customerId}/source/{sourceService}
     * Retrieve notifications for a customer filtered by source service.
     */
    @GetMapping("/customer/{customerId}/source/{sourceService}")
    public ResponseEntity<List<Notification>> getByCustomerAndSource(
            @PathVariable UUID customerId,
            @PathVariable SourceService sourceService) {

        logger.info("GET /customer/{}/source/{} - Fetching filtered notifications", customerId, sourceService);
        List<Notification> notifications = notificationService
                .getNotificationsByCustomerAndSource(customerId, sourceService);
        logger.info("Returning {} notifications for customerId={} from source={}",
                notifications.size(), customerId, sourceService);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/reference/{referenceId}
     * Retrieve all notifications linked to a reference entity (account number, loan ID, card ID).
     */
    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<List<Notification>> getByReferenceId(@PathVariable String referenceId) {
        logger.info("GET /reference/{} - Fetching notifications by reference", referenceId);
        List<Notification> notifications = notificationService.getNotificationsByReferenceId(referenceId);
        logger.info("Returning {} notifications for referenceId={}", notifications.size(), referenceId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/date-range?start=...&end=...
     * Retrieve notifications created within a date range.
     *
     * <p>Date format: {@code yyyy-MM-dd'T'HH:mm:ss} (e.g., 2026-05-01T00:00:00)</p>
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Notification>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        logger.info("GET /date-range?start={}&end={} - Fetching notifications in range", start, end);
        List<Notification> notifications = notificationService.getNotificationsByDateRange(start, end);
        logger.info("Returning {} notifications in date range [{} to {}]", notifications.size(), start, end);
        return ResponseEntity.ok(notifications);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ANALYTICS ENDPOINTS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/notifications/analytics/count-by-status/{status}
     * Get the total count of notifications with a specific status.
     * Useful for monitoring dashboards.
     */
    @GetMapping("/analytics/count-by-status/{status}")
    public ResponseEntity<Map<String, Object>> countByStatus(@PathVariable NotificationStatus status) {
        logger.info("GET /analytics/count-by-status/{} - Counting notifications", status);
        long count = notificationService.countByStatus(status);
        logger.info("Count for status={}: {}", status, count);
        return ResponseEntity.ok(Map.of("status", status, "count", count));
    }

    /**
     * GET /api/v1/notifications/analytics/count-by-source/{sourceService}
     * Get the total count of notifications from a specific source service.
     * Useful for service analytics.
     */
    @GetMapping("/analytics/count-by-source/{sourceService}")
    public ResponseEntity<Map<String, Object>> countBySource(@PathVariable SourceService sourceService) {
        logger.info("GET /analytics/count-by-source/{} - Counting notifications", sourceService);
        long count = notificationService.countBySourceService(sourceService);
        logger.info("Count for sourceService={}: {}", sourceService, count);
        return ResponseEntity.ok(Map.of("sourceService", sourceService, "count", count));
    }
}
