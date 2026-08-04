package com.retail.notificationservice.dto.request;

import com.retail.notificationservice.enums.NotificationType;
import com.retail.notificationservice.enums.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    private String subject;

    @NotBlank(message = "Message content is required")
    private String message;

    private ReferenceType referenceType;

    private Long referenceId;
}
