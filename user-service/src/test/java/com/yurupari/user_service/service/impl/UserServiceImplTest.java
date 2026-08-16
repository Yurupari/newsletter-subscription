package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.exception.AuthenticationException;
import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import com.yurupari.user_service.model.entity.User;
import com.yurupari.user_service.model.enums.UserStatus;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.request.UserUpdateRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.model.mapper.UserMapperImpl;
import com.yurupari.user_service.repository.UserRepository;
import com.yurupari.user_service.service.AuthenticationService;
import com.yurupari.user_service.service.EncryptionService;
import com.yurupari.user_service.service.UserValidationService;
import com.yurupari.user_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapperImpl userMapper = new UserMapperImpl();

    private UserRequest userRequest;
    private LoginRequest loginRequest;
    private UserUpdateRequest userUpdateRequest;
    private User user, deletedUser;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        userRequest = TestModelFactory.createUserRequest(
                "test@email.com",
                "testPassword",
                "testName",
                "testLastName"
        );

        loginRequest = TestModelFactory.createLoginRequest(
                "test@email.com",
                "testPassword"
        );

        userUpdateRequest = TestModelFactory.createUserUpdateRequest(
                "testPassword",
                "testName",
                "testLastName"
        );

        authResponse = TestModelFactory.createAuthenticationResponse(
                "accessToken",
                "refreshToken",
                "Bearer",
                3600L
        );

        user = TestModelFactory.createUser(
                1L,
                "test@email.com",
                "encryptedPassword",
                "testName",
                "testLastName",
                UserStatus.ACTIVE,
                1L,
                Instant.now(),
                Instant.now()
        );

        deletedUser = TestModelFactory.createUser(
                1L,
                "test@email.com",
                "encryptedPassword",
                "testName",
                "testLastName",
                UserStatus.DELETED,
                1L,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void registerUser_Success_UserDoNoExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(encryptionService.encrypt(anyString())).thenReturn("encryptedPassword");
        when(userRepository.saveAndFlush(any())).thenReturn(user);

        var response = userService.registerUser(userRequest);

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getFirstName(), response.firstName());
        assertEquals(user.getLastName(), response.lastName());
    }

    @Test
    void registerUser_Success_UserAlreadyExistsButDeleted() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(deletedUser));
        when(encryptionService.encrypt(anyString())).thenReturn("encryptedPassword");
        when(userRepository.saveAndFlush(any())).thenReturn(user);

        var response = userService.registerUser(userRequest);

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getFirstName(), response.firstName());
        assertEquals(user.getLastName(), response.lastName());
    }

    @Test
    void registerUser_Fail_UserAlreadyExistsActive() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(userRequest));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.email().toLowerCase())).thenReturn(Optional.of(user));
        when(authenticationService.authenticate(loginRequest)).thenReturn(authResponse);

        var response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals(authResponse.accessToken(), response.accessToken());
        verify(userValidationService).validateUser(user.getPassword(), loginRequest.password());
    }

    @Test
    void login_Fail_UserNotFound() {
        when(userRepository.findByEmail(loginRequest.email().toLowerCase())).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> userService.login(loginRequest));
    }

    @Test
    void login_Fail_InactiveUser() {
        when(userRepository.findByEmail(loginRequest.email().toLowerCase())).thenReturn(Optional.of(deletedUser));

        assertThrows(AuthenticationException.class, () -> userService.login(loginRequest));
    }

    @Test
    void login_Fail_WrongPassword() {
        when(userRepository.findByEmail(loginRequest.email().toLowerCase())).thenReturn(Optional.of(user));
        doThrow(new AuthenticationException("Invalid email or password"))
                .when(userValidationService).validateUser(user.getPassword(), loginRequest.password());

        assertThrows(AuthenticationException.class, () -> userService.login(loginRequest));
    }

    @Test
    void getUser_Success_WithEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), user.getEmail())).thenReturn(List.of(user));

        var response = userService.getUser(user.getId(), user.getEmail());

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
        verify(userValidationService).validateUserIdAndEmail(user.getId(), user.getEmail());
    }

    @Test
    void getUser_Success_WithNullEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), null)).thenReturn(List.of(user));

        var response = userService.getUser(user.getId(), null);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
        verify(userValidationService).validateUserIdAndEmail(user.getId(), null);
    }

    @Test
    void getUser_Fail_UserNotFound() {
        when(userRepository.findByIdOrEmail(any(), anyString())).thenReturn(Collections.emptyList());
        doThrow(UserNotFoundException.class)
                .when(userValidationService)
                .validateUsers(anyLong(), anyString(), any());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(user.getId(), user.getEmail()));
    }

    @Test
    void getUser_Fail_InactiveUser() {
        when(userRepository.findByIdOrEmail(any(), anyString())).thenReturn(List.of(deletedUser));
        doThrow(UserNotFoundException.class)
                .when(userValidationService)
                .validateUsers(anyLong(), anyString(), any());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(user.getId(), user.getEmail()));
    }

    @Test
    void updateUser_Success_WithEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), user.getEmail())).thenReturn(List.of(user));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.updateUser(user.getId(), user.getEmail(), userUpdateRequest);

        assertNotNull(response);
        assertEquals(userUpdateRequest.firstName(), response.firstName());
        assertEquals(userUpdateRequest.lastName(), response.lastName());
        verify(userMapper).updateEntityFromUserRequest(userUpdateRequest, user);
    }

    @Test
    void updateUser_Success_WithNullEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), null)).thenReturn(List.of(user));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.updateUser(user.getId(), null, userUpdateRequest);

        assertNotNull(response);
        assertEquals(userUpdateRequest.firstName(), response.firstName());
        assertEquals(userUpdateRequest.lastName(), response.lastName());
        verify(userMapper).updateEntityFromUserRequest(userUpdateRequest, user);
    }

    @Test
    void updateUser_Fail_UserNotFound() {
        when(userRepository.findByIdOrEmail(any(), anyString())).thenReturn(Collections.emptyList());
        doThrow(UserNotFoundException.class)
                .when(userValidationService)
                .validateUsers(anyLong(), anyString(), any());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(user.getId(), user.getEmail(), userUpdateRequest));
    }

    @Test
    void deleteUser_Success_WithEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), user.getEmail())).thenReturn(List.of(user));

        userService.deleteUser(user.getId(), user.getEmail());

        assertEquals(UserStatus.DELETED, user.getStatus());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void deleteUser_Success_WithNullEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), null)).thenReturn(List.of(user));

        userService.deleteUser(user.getId(), null);

        assertEquals(UserStatus.DELETED, user.getStatus());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void deleteUser_Fail_UserNotFound() {
        when(userRepository.findByIdOrEmail(any(), anyString())).thenReturn(Collections.emptyList());
        doThrow(UserNotFoundException.class)
                .when(userValidationService)
                .validateUsers(anyLong(), anyString(), any());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(user.getId(), user.getEmail()));
    }
}
