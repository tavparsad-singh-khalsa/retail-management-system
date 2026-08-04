package com.retail.notificationservice.service;

import com.retail.notificationservice.dto.request.CreateNotificationRequest;
import com.retail.notificationservice.dto.request.SendTestNotificationRequest;
import com.retail.notificationservice.dto.response.NotificationDashboardCounts;
import com.retail.notificationservice.dto.response.NotificationResponse;
import com.retail.notificationservice.dto.response.NotificationSummaryResponse;
import com.retail.notificationservice.enums.NotificationStatus;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotification(Long id);

    NotificationResponse getNotificationByNumber(String notificationNumber);

    List<NotificationSummaryResponse> getAllNotifications();

    List<NotificationSummaryResponse> getNotificationsByRecipient(String recipient);

    List<NotificationSummaryResponse> getNotificationsByStatus(NotificationStatus status);

    NotificationResponse retryNotification(Long id);

    NotificationResponse sendTestNotification(SendTestNotificationRequest request);

    NotificationResponse markAsSent(Long id);

    NotificationResponse markAsFailed(Long id);

    NotificationResponse deactivateNotification(Long id);

    NotificationResponse activateNotification(Long id);

    NotificationDashboardCounts getDashboardCounts();
}
