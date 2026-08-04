package com.retail.reportservice.dto.external;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalNotificationCountsDto {

    private long pending;
    private long sent;
    private long failed;
}
