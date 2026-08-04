package com.retail.notificationservice.controller;

import com.retail.notificationservice.dto.request.CreateNotificationRequest;
import com.retail.notificationservice.dto.request.SendTestNotificationRequest;
import com.retail.notificationservice.dto.response.NotificationDashboardCounts;
import com.retail.notificationservice.dto.response.NotificationResponse;
import com.retail.notificationservice.dto.response.NotificationSummaryResponse;
import com.retail.notificationservice.enums.NotificationStatus;
import com.retail.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotification(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<NotificationResponse> getNotificationByNumber(@PathVariable String number) {
        NotificationResponse response = notificationService.getNotificationByNumber(number);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationSummaryResponse>> getAllNotifications() {
        List<NotificationSummaryResponse> responses = notificationService.getAllNotifications();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<NotificationSummaryResponse>> getNotificationsByRecipient(@PathVariable String recipient) {
        List<NotificationSummaryResponse> responses = notificationService.getNotificationsByRecipient(recipient);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationSummaryResponse>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        List<NotificationSummaryResponse> responses = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/test")
    public ResponseEntity<NotificationResponse> sendTestNotification(@Valid @RequestBody SendTestNotificationRequest request) {
        NotificationResponse response = notificationService.sendTestNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/retry")
    public ResponseEntity<NotificationResponse> retryNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.retryNotification(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/sent")
    public ResponseEntity<NotificationResponse> markAsSent(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsSent(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/failed")
    public ResponseEntity<NotificationResponse> markAsFailed(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsFailed(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<NotificationResponse> activateNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.activateNotification(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<NotificationResponse> deactivateNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.deactivateNotification(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/counts")
    public ResponseEntity<NotificationDashboardCounts> getDashboardCounts() {
        NotificationDashboardCounts counts = notificationService.getDashboardCounts();
        return ResponseEntity.ok(counts);
    }
}
