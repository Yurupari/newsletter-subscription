package com.yurupari.user_service.client;

import com.yurupari.user_service.model.http.request.KeycloakUserRepresentationRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = {MediaType.APPLICATION_JSON_VALUE})
public interface KeycloakClient {

    @Retry(name = "keycloakAuth")
    @PostExchange(
            url = "/admin/realms/{realm}/users",
            contentType = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> createUser(
            @PathVariable("realm") String realm,
            @RequestHeader("Authorization") String adminBearerToken,
            @RequestBody KeycloakUserRepresentationRequest user
    );

    @Retry(name = "keycloakAuth")
    @PostExchange(
            url = "/realms/{realm}/protocol/openid-connect/token",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    AuthenticationResponse authenticateUser(
            @PathVariable("realm") String realm,
            @RequestHeader("Authorization") String authorization,
            @RequestBody MultiValueMap<String, String> formData
    );

    @Retry(name = "keycloakAuth")
    @PostExchange(
            url = "/realms/{realm}/protocol/openid-connect/token",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    AuthenticationResponse authenticateClient(
            @PathVariable("realm") String realm,
            @RequestHeader("Authorization") String authorization,
            @RequestBody MultiValueMap<String, String> formData
    );

    @DeleteExchange(url = "/admin/realms/{realm}/users/{keycloakUserId}")
    void deleteUser(
            @PathVariable("realm") String realm,
            @PathVariable("keycloakUserId") String keycloakUserId,
            @RequestHeader("Authorization") String adminBearerToken
    );
}
