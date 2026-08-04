package com.retail.notificationservice.repository;

import com.retail.notificationservice.entity.Notification;
import com.retail.notificationservice.enums.NotificationStatus;
import com.retail.notificationservice.enums.NotificationType;
import com.retail.notificationservice.enums.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationNumber(String notificationNumber);

    List<Notification> findByRecipient(String recipient);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByNotificationType(NotificationType notificationType);

    List<Notification> findByReferenceTypeAndReferenceId(ReferenceType referenceType, Long referenceId);

    List<Notification> findByActiveTrue();

    List<Notification> findByStatusAndActiveTrue(NotificationStatus status);

    boolean existsByNotificationNumber(String notificationNumber);

    // Dashboard / Reporting Queries
    long countByStatus(NotificationStatus status);

    long countByNotificationType(NotificationType notificationType);

    long countByReferenceType(ReferenceType referenceType);
}
