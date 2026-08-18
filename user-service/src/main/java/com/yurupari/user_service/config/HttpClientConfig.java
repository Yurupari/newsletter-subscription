package com.yurupari.user_service.config;

import com.yurupari.common_data.exception.ApiServerException;
import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.exception.KeycloakClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Configuration
@EnableAspectJAutoProxy
@Slf4j
public class HttpClientConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Bean
    public KeycloakClient keycloakClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        var restClient = RestClient.builder()
                .baseUrl(URI.create(keycloakUrl))
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    var username = Optional.ofNullable(req.getAttributes().get("username"))
                            .map(Object::toString)
                            .orElse("unknown");
                    var responseBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    var statusCode = res.getStatusCode();

                    log.error("Keycloak 4xx error: username={}, status={}, body={}",
                            username, statusCode, responseBody);

                    throw new KeycloakClientException(username);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    var username = Optional.ofNullable(req.getAttributes().get("username"))
                            .map(Object::toString)
                            .orElse("unknown");
                    var responseBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    var statusCode = res.getStatusCode();

                    log.error("Keycloak 5xx error: username={}, status={}, body={}",
                            username, statusCode, responseBody);

                    throw new ApiServerException(String.format("Error calling Keycloak API: email=%s", username));
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(KeycloakClient.class);
    }
}
