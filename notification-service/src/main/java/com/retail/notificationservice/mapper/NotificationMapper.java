package com.retail.notificationservice.mapper;

import com.retail.notificationservice.dto.request.CreateNotificationRequest;
import com.retail.notificationservice.dto.response.NotificationResponse;
import com.retail.notificationservice.dto.response.NotificationSummaryResponse;
import com.retail.notificationservice.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationMapper {

    public Notification toEntity(CreateNotificationRequest request) {
        if (request == null) {
            return null;
        }

        return Notification.builder()
                .notificationType(request.getNotificationType())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .build();
    }

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationNumber(notification.getNotificationNumber())
                .notificationType(notification.getNotificationType())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .active(notification.getActive())
                .build();
    }

    public NotificationSummaryResponse toSummaryResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationSummaryResponse.builder()
                .id(notification.getId())
                .notificationNumber(notification.getNotificationNumber())
                .notificationType(notification.getNotificationType())
                .recipient(notification.getRecipient())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null) {
            return List.of();
        }

        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationSummaryResponse> toSummaryList(List<Notification> notifications) {
        if (notifications == null) {
            return List.of();
        }

        return notifications.stream()
                .map(this::toSummaryResponse)
                .toList();
    }
}
