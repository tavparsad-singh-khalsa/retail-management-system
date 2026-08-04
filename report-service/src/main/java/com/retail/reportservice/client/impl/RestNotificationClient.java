package com.retail.reportservice.client.impl;

import com.retail.reportservice.client.NotificationClient;
import com.retail.reportservice.dto.external.ExternalNotificationCountsDto;
import com.retail.reportservice.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestNotificationClient implements NotificationClient {

    private final RestClient notificationRestClient;

    @Override
    public ExternalNotificationCountsDto getDashboardCounts() {
        log.info("Fetching notification counts from Notification Service...");
        try {
            ExternalNotificationCountsDto counts = notificationRestClient.get()
                    .uri("/api/v1/notifications/dashboard/counts")
                    .retrieve()
                    .body(ExternalNotificationCountsDto.class);
            return counts != null ? counts : new ExternalNotificationCountsDto();
        } catch (RestClientException e) {
            log.error("Failed to fetch notification counts from Notification Service", e);
            throw new ExternalServiceException("Notification Service communication failure", e);
        }
    }
}
