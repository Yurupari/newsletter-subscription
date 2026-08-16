package com.yurupari.user_service.service;

import com.yurupari.common_data.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;

public interface AuthenticationService {

    void createUser(RegisterUserEvent registerUserEvent);

    AuthenticationResponse authenticate(LoginRequest loginRequest);
}
