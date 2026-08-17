package com.yurupari.user_service.service;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.request.UserUpdateRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.model.http.response.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest userRequest);

    void activateUser(RegisterUserEvent registerUserEvent);

    AuthenticationResponse login(LoginRequest loginRequest);

    UserResponse getUser(Long id, String email);

    UserResponse updateUser(Long id, String email, UserUpdateRequest updateRequest);

    void deleteUser(Long id, String email);

    void deactivateUser(DeleteUserEvent deleteUserEvent);
}
