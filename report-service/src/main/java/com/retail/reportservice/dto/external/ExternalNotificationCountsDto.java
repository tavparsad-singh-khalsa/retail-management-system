package com.retail.reportservice.dto.external;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalNotificationCountsDto {

    private long pendingCount;
    private long sentCount;
    private long failedCount;
    private long totalCount;
}
