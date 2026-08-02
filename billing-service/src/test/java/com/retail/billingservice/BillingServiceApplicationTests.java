package com.retail.billingservice;

import com.retail.billingservice.service.BillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BillingServiceApplicationTests {

    @Autowired
    private BillingService billingService;

    @Test
    void contextLoads() {
        assertThat(billingService).isNotNull();
    }

}
