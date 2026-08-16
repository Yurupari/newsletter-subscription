package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.exception.AuthenticationException;
import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import com.yurupari.user_service.model.dto.UserDto;
import com.yurupari.user_service.model.enums.UserStatus;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.request.UserUpdateRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.model.http.response.UserResponse;
import com.yurupari.user_service.model.mapper.UserMapper;
import com.yurupari.user_service.repository.UserRepository;
import com.yurupari.user_service.service.AuthenticationService;
import com.yurupari.user_service.service.EncryptionService;
import com.yurupari.user_service.service.UserService;
import com.yurupari.user_service.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserValidationService userValidationService;

    private final EncryptionService encryptionService;

    private final AuthenticationService authenticationService;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse registerUser(UserRequest userRequest) {
        log.info("Register user: email={}, firstName={}, lastName={}",
                userRequest.email(), userRequest.firstName(), userRequest.lastName());

        return userRepository.findByEmail(userRequest.email().toLowerCase())
                .map(user -> {
                    var existingStatus = List.of(UserStatus.ACTIVE, UserStatus.PENDING_ACTIVATION);
                    if (existingStatus.contains(user.getStatus())) {
                        throw new UserAlreadyExistsException(userRequest.email());
                    }

                    var userDto = buildUserDto(userRequest);

                    userMapper.updateEntityFromDto(userDto, user);
                    user.setStatus(UserStatus.ACTIVE);

                    var savedUser = userRepository.saveAndFlush(user);

                    return userMapper.toUserResponse(savedUser);
                })
                .orElseGet(() -> {
                    var userDto = buildUserDto(userRequest);

                    var savedUser = userRepository.saveAndFlush(userMapper.toEntity(userDto));

                    return userMapper.toUserResponse(savedUser);
                });
    }

    @Override
    public void activateUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setStatus(UserStatus.ACTIVE);

        userRepository.saveAndFlush(user);
    }

    @Override
    public AuthenticationResponse login(LoginRequest loginRequest) {
        log.info("Login user: email={}", loginRequest.email());

        var email = loginRequest.email().toLowerCase();

        var user = userRepository.findByEmail(email)
                .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        userValidationService.validateUser(user.getPassword(), loginRequest.password());

        return authenticationService.authenticate(loginRequest);
    }

    @Override
    public UserResponse getUser(Long id, String email) {
        log.info("Get user: id={}, email={}", id, email);

        userValidationService.validateUserIdAndEmail(id, email);

        var formattedEmail = Optional.ofNullable(email)
                .map(String::toLowerCase)
                .orElse(null);

        var users = userRepository.findByIdOrEmail(id, formattedEmail).stream()
                .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                .toList();

        userValidationService.validateUsers(id, formattedEmail, users);

        return userMapper.toUserResponse(users.getFirst());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, String email, UserUpdateRequest updateRequest) {
        log.info("Update user: id={}, email={}", id, email);

        userValidationService.validateUserIdAndEmail(id, email);

        var formattedEmail = Optional.ofNullable(email)
                .map(String::toLowerCase)
                .orElse(null);

        var users = userRepository.findByIdOrEmail(id, formattedEmail).stream()
                .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                .toList();

        userValidationService.validateUsers(id, formattedEmail, users);
        var user = users.getFirst();

        userMapper.updateEntityFromUserRequest(updateRequest, user);

        var updatedUser = userRepository.saveAndFlush(user);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String email) {
        log.info("Delete user: id={}, email={}", id, email);

        userValidationService.validateUserIdAndEmail(id, email);

        var formattedEmail = Optional.ofNullable(email)
                .map(String::toLowerCase)
                .orElse(null);

        var users = userRepository.findByIdOrEmail(id, formattedEmail).stream()
                .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                .toList();

        userValidationService.validateUsers(id, formattedEmail, users);
        var user = users.getFirst();

        user.setStatus(UserStatus.DELETED);

        userRepository.saveAndFlush(user);
    }

    private UserDto buildUserDto(UserRequest userRequest) {
        var encryptedPassword = encryptionService.encrypt(userRequest.password());

        return UserDto.builder()
                .email(userRequest.email().toLowerCase())
                .password(encryptedPassword)
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .build();
    }
}
