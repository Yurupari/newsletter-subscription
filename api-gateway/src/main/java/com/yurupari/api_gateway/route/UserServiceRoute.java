package com.yurupari.api_gateway.route;

import com.yurupari.api_gateway.config.GatewayConfigProperties;
import com.yurupari.api_gateway.route.factory.GatewayRouteFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static com.yurupari.api_gateway.model.enums.GatewayService.USER;

@Configuration
@RequiredArgsConstructor
public class UserServiceRoute {

    private final GatewayConfigProperties properties;
    private final GatewayRouteFactory routeFactory;

    @Bean
    public RouterFunction<ServerResponse> userRoute() {
        var userServiceConfig = properties.items().get(USER.getName());

        return routeFactory.createServiceRoute(
                USER.getName(),
                userServiceConfig.path(),
                userServiceConfig.url(),
                userServiceConfig.circuitBreakerId(),
                userServiceConfig.fallbackPath()
        );
    }

    @Bean
    public RouterFunction<ServerResponse> userFallbackRoute() {
        var userServiceConfig = properties.items().get(USER.getName());

        return routeFactory.createFallbackRoute(
                userServiceConfig.fallbackMessage(),
                userServiceConfig.fallbackPath()
        );
    }
}
