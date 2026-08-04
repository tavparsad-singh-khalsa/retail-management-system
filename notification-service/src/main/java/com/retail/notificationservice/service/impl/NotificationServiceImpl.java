package com.retail.notificationservice.service.impl;

import com.retail.notificationservice.dto.request.CreateNotificationRequest;
import com.retail.notificationservice.dto.request.SendTestNotificationRequest;
import com.retail.notificationservice.dto.response.NotificationDashboardCounts;
import com.retail.notificationservice.dto.response.NotificationResponse;
import com.retail.notificationservice.dto.response.NotificationSummaryResponse;
import com.retail.notificationservice.entity.Notification;
import com.retail.notificationservice.enums.NotificationStatus;
import com.retail.notificationservice.exception.NotificationNotFoundException;
import com.retail.notificationservice.exception.NotificationValidationException;
import com.retail.notificationservice.mapper.NotificationMapper;
import com.retail.notificationservice.repository.NotificationRepository;
import com.retail.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for recipient: {}, type: {}", request.getRecipient(),
                request.getNotificationType());

        Notification notification = notificationMapper.toEntity(request);
        notification.setNotificationNumber(generateNotificationNumber());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setActive(true);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created successfully with number: {}", saved.getNotificationNumber());

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long id) {
        log.info("Fetching notification by ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationByNumber(String notificationNumber) {
        log.info("Fetching notification by number: {}", notificationNumber);
        Notification notification = notificationRepository.findByNotificationNumber(notificationNumber)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found with number: " + notificationNumber));

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSummaryResponse> getAllNotifications() {
        log.info("Fetching all notifications");
        List<Notification> notifications = notificationRepository.findAll();
        return notificationMapper.toSummaryList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSummaryResponse> getNotificationsByRecipient(String recipient) {
        log.info("Fetching notifications for recipient: {}", recipient);
        List<Notification> notifications = notificationRepository.findByRecipient(recipient);
        return notificationMapper.toSummaryList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSummaryResponse> getNotificationsByStatus(NotificationStatus status) {
        log.info("Fetching notifications with status: {}", status);
        List<Notification> notifications = notificationRepository.findByStatus(status);
        return notificationMapper.toSummaryList(notifications);
    }

    @Override
    @Transactional
    public NotificationResponse retryNotification(Long id) {
        log.info("Retrying notification ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!Boolean.TRUE.equals(notification.getActive())) {
            throw new NotificationValidationException("Cannot retry inactive notification ID: " + id);
        }

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new NotificationValidationException(
                    "Only FAILED notifications can be retried. Current status: " + notification.getStatus());
        }

        // Reset status transition: FAILED -> PENDING -> SENT
        notification.setStatus(NotificationStatus.PENDING);
        log.info("Reset status to PENDING for retry on notification ID: {}", id);

        // Simulate sending operation
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);
        log.info("Notification ID: {} successfully retried and marked SENT", id);

        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public NotificationResponse sendTestNotification(SendTestNotificationRequest request) {
        log.info("Sending test notification to recipient: {}, type: {}", request.getRecipient(),
                request.getNotificationType());

        Notification notification = Notification.builder()
                .notificationNumber(generateNotificationNumber())
                .notificationType(request.getNotificationType())
                .recipient(request.getRecipient())
                .subject("Test Notification")
                .message(request.getMessage())
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .active(true)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Test notification sent successfully with number: {}", saved.getNotificationNumber());

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NotificationResponse markAsSent(Long id) {
        log.info("Marking notification ID: {} as SENT", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!Boolean.TRUE.equals(notification.getActive())) {
            throw new NotificationValidationException("Cannot mark inactive notification as SENT: " + id);
        }

        if (notification.getStatus() == NotificationStatus.SENT) {
            log.info("Notification ID: {} is already SENT", id);
            return notificationMapper.toResponse(notification);
        }

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public NotificationResponse markAsFailed(Long id) {
        log.info("Marking notification ID: {} as FAILED", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        if (!Boolean.TRUE.equals(notification.getActive())) {
            throw new NotificationValidationException("Cannot mark inactive notification as FAILED: " + id);
        }

        notification.setStatus(NotificationStatus.FAILED);

        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public NotificationResponse deactivateNotification(Long id) {
        log.info("Deactivating notification ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        notification.setActive(false);

        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public NotificationResponse activateNotification(Long id) {
        log.info("Activating notification ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        notification.setActive(true);

        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDashboardCounts getDashboardCounts() {
        return NotificationDashboardCounts.builder()
                .pending(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .sent(notificationRepository.countByStatus(NotificationStatus.SENT))
                .failed(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .build();
    }

    private String generateNotificationNumber() {
        long next = notificationRepository.count() + 1;
        return "NOT-" + Year.now().getValue() + "-" + String.format("%06d", next);
    }
}
