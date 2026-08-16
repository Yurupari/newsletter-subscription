package com.yurupari.user_service.config;

import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.exception.KeycloakClientException;
import com.yurupari.user_service.exception.ApiServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Configuration
@EnableAspectJAutoProxy
public class HttpClientConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public KeycloakClient keycloakClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        var basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        var restClient = RestClient.builder()
                .baseUrl(URI.create(keycloakUrl))
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    var username = Optional.ofNullable(req.getAttributes().get("username"))
                            .map(Object::toString)
                            .orElse("unknown");
                    throw new KeycloakClientException(username);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    var username = Optional.ofNullable(req.getAttributes().get("username"))
                            .map(Object::toString)
                            .orElse("unknown");
                    throw new ApiServerException(String.format("Error calling Keycloak API: email=%s", username));
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(KeycloakClient.class);
    }
}
