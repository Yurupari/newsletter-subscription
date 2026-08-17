package com.yurupari.user_service.service;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;

public interface AuthenticationService {

    String createUser(RegisterUserEvent registerUserEvent);

    AuthenticationResponse authenticate(LoginRequest loginRequest);

    void deleteUser(DeleteUserEvent deleteUserEvent);
}
