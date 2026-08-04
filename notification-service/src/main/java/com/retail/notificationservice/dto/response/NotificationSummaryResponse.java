package com.retail.notificationservice.dto.response;

import com.retail.notificationservice.enums.NotificationStatus;
import com.retail.notificationservice.enums.NotificationType;
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
public class NotificationSummaryResponse {

    private Long id;
    private String notificationNumber;
    private NotificationType notificationType;
    private String recipient;
    private NotificationStatus status;
    private LocalDateTime createdAt;
}
