package com.yurupari.api_gateway.route;

import com.yurupari.api_gateway.config.GatewayConfigProperties;
import com.yurupari.api_gateway.route.factory.GatewayRouteFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static com.yurupari.api_gateway.model.enums.GatewayService.SUBSCRIPTION;

@Configuration
@RequiredArgsConstructor
public class SubscriptionServiceRoute {

    private final GatewayConfigProperties properties;

    private final GatewayRouteFactory routeFactory;

    @Bean
    public RouterFunction<ServerResponse> subscriptionRoute() {
        var subscriptionServiceConfig = properties.items().get(SUBSCRIPTION.getName());

        return routeFactory.createServiceRoute(
                SUBSCRIPTION.getName(),
                subscriptionServiceConfig.path(),
                subscriptionServiceConfig.url(),
                subscriptionServiceConfig.circuitBreakerId(),
                subscriptionServiceConfig.fallbackPath()
        );
    }

    @Bean
    public RouterFunction<ServerResponse> subscriptionFallbackRoute() {
        var subscriptionServiceConfig = properties.items().get(SUBSCRIPTION.getName());

        return routeFactory.createFallbackRoute(
                subscriptionServiceConfig.fallbackMessage(),
                subscriptionServiceConfig.fallbackPath()
        );
    }
}
