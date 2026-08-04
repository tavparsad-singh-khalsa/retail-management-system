package com.retail.reportservice.client;

import com.retail.reportservice.dto.external.ExternalNotificationCountsDto;

public interface NotificationClient {

    ExternalNotificationCountsDto getDashboardCounts();
}
