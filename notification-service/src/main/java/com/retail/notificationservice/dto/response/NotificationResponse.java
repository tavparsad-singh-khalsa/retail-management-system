package com.retail.notificationservice.dto.response;

import com.retail.notificationservice.enums.NotificationStatus;
import com.retail.notificationservice.enums.NotificationType;
import com.retail.notificationservice.enums.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String notificationNumber;
    private NotificationType notificationType;
    private String recipient;
    private String subject;
    private String message;
    private NotificationStatus status;
    private ReferenceType referenceType;
    private Long referenceId;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}
