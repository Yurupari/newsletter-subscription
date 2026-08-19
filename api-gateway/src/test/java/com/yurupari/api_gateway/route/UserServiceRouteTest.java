package com.yurupari.api_gateway.route;

import com.yurupari.api_gateway.config.GatewayConfigProperties;
import com.yurupari.api_gateway.model.ServiceConfig;
import com.yurupari.api_gateway.route.factory.GatewayRouteFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.function.RouterFunction;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceRouteTest {

    private UserServiceRoute userServiceRoute;

    private GatewayConfigProperties properties;

    @Mock
    private GatewayRouteFactory routeFactory;

    @BeforeEach
    void setUp() {
        Map<String, ServiceConfig> propertyMap = new HashMap<>();
        propertyMap.put("user-service", new ServiceConfig(
                "http://localhost:9999",
                "/api/v1/user/**",
                "userServiceCircuitBreaker",
                "/fallback/user",
                "User service is down"
        ));

        properties = new GatewayConfigProperties(propertyMap);

        userServiceRoute = new UserServiceRoute(properties, routeFactory);
    }

    @Test
    void userRoute() {
        var routerFunction = mock(RouterFunction.class);
        when(routeFactory.createServiceRoute(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(routerFunction);

        var response = userServiceRoute.userRoute();

        assertNotNull(response);
        assertEquals(routerFunction, response);
        verify(routeFactory).createServiceRoute(
                eq("user-service"),
                eq("/api/v1/user/**"),
                eq("http://localhost:9999"),
                eq("userServiceCircuitBreaker"),
                anyString()
        );
    }

    @Test
    void userFallbackRoute() {
        var routerFunction = mock(RouterFunction.class);
        when(routeFactory.createFallbackRoute(anyString(), anyString())).thenReturn(routerFunction);

        var response = userServiceRoute.userFallbackRoute();

        assertNotNull(response);
        assertEquals(routerFunction, response);
        verify(routeFactory).createFallbackRoute(
                anyString(),
                eq("/fallback/user")
        );
    }
}