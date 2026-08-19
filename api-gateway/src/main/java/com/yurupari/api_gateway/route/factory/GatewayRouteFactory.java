package com.yurupari.api_gateway.route.factory;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Component
public class GatewayRouteFactory {

    public RouterFunction<ServerResponse> createServiceRoute(
            String serviceId,
            String path,
            String url,
            String circuitBreakerId,
            String fallbackPath
    ) {
        return route(serviceId)
                .route(RequestPredicates.path(path), http())
                .before(uri(url))
                .filter((CircuitBreakerFilterFunctions.circuitBreaker(
                        circuitBreakerId,
                        URI.create("forward:" + fallbackPath)
                )))
                .build();
    }

    public RouterFunction<ServerResponse> createFallbackRoute(String message, String fallbackPath) {
        return route(fallbackPath)
                .route(RequestPredicates.path(fallbackPath),
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(message))
                .build();
    }
}
