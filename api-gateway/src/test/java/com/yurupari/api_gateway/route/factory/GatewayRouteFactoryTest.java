package com.yurupari.api_gateway.route.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GatewayRouteFactoryTest {

    @InjectMocks
    private GatewayRouteFactory gatewayRouteFactory;

    @Test
    void createServiceRoute() {
        var response = gatewayRouteFactory.createServiceRoute(
                "TEST_SERVICE_ID",
                "/api/v1/test-service",
                "http://localhost:9999",
                "testServiceCircuitBreaker",
                "/fallback/test-service"
        );

        assertNotNull(response);
    }

    @Test
    void createFallbackRoute() {
        var response = gatewayRouteFactory.createFallbackRoute(
                "TEST_FALLBACK",
                "/fallback/test-service"
        );

        assertNotNull(response);
    }
}