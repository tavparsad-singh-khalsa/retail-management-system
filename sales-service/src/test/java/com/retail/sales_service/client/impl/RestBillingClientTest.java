package com.retail.sales_service.client.impl;

import com.retail.sales_service.dto.integration.response.InvoiceSettingsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestBillingClientTest {

    @Test
    void requestsActiveInvoiceFromBillingService() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://billing-service.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestBillingClient client = new RestBillingClient(builder.build());

        server.expect(once(), requestTo("http://billing-service.test/api/v1/invoices/sale/42"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertTrue(client.hasActiveInvoice(42L));
        server.verify();
    }

    @Test
    void fetchesInvoiceSettingsFromBillingService() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://billing-service.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestBillingClient client = new RestBillingClient(builder.build());

        server.expect(once(), requestTo("http://billing-service.test/api/v1/invoice-settings"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "{\"taxEnabled\":true,\"taxName\":\"GST\",\"taxRate\":18.00}",
                        MediaType.APPLICATION_JSON));

        InvoiceSettingsResponse settings = client.getInvoiceSettings();
        assertEquals(true, settings.getTaxEnabled());
        assertEquals("GST", settings.getTaxName());
        assertEquals(0, new BigDecimal("18.00").compareTo(settings.getTaxRate()));
        server.verify();
    }

    @Test
    void defaultsToTaxDisabledWhenInvoiceSettingsServiceFails() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://billing-service.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestBillingClient client = new RestBillingClient(builder.build());

        server.expect(once(), requestTo("http://billing-service.test/api/v1/invoice-settings"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        InvoiceSettingsResponse settings = client.getInvoiceSettings();
        assertEquals(false, settings.getTaxEnabled());
        assertEquals(0, BigDecimal.ZERO.compareTo(settings.getTaxRate()));
        server.verify();
    }
}
