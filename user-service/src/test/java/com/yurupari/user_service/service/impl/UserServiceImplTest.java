package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.exception.AuthenticationException;
import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.messaging.kafka.UserProducer;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private UserProducer userProducer;

    @Spy
    private UserMapperImpl userMapper = new UserMapperImpl();

    private UserRequest userRequest;
    private LoginRequest loginRequest;
    private UserUpdateRequest userUpdateRequest;
    private User user, deletedUser, pendingActivationUser, pendingDeletionUser, failedActivationUser;
    private AuthenticationResponse authResponse;
    private RegisterUserEvent registerUserEvent;
    private DeleteUserEvent deleteUserEvent;

    @BeforeEach
    void setUp() {
        userRequest = TestModelFactory.createUserRequest(
                "test@email.com",
                "testPassword",
                "testName",
                "testLastName");

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
                "authUserId",
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
                null,
                1L,
                Instant.now(),
                Instant.now()
        );

        pendingActivationUser = TestModelFactory.createUser(
                1L,
                "test@email.com",
                "encryptedPassword",
                "testName",
                "testLastName",
                UserStatus.PENDING_ACTIVATION,
                null,
                1L,
                Instant.now(),
                Instant.now()
        );

        pendingDeletionUser = TestModelFactory.createUser(
                1L,
                "test@email.com",
                "encryptedPassword",
                "testName",
                "testLastName",
                UserStatus.PENDING_DELETION,
                "authUserId",
                1L,
                Instant.now(),
                Instant.now()
        );

        failedActivationUser = TestModelFactory.createUser(
                1L,
                "test@email.com",
                "encryptedPassword",
                "testName",
                "testLastName",
                UserStatus.FAILED_ACTIVATION,
                null,
                1L,
                Instant.now(),
                Instant.now()
        );

        registerUserEvent = TestModelFactory.createRegisterUserEvent(
                1L,
                "test@email.com",
                "testPassword",
                "testName",
                "testLastName"
        );

        deleteUserEvent = TestModelFactory.createDeleteUserEvent(
                1L,
                "authUserId"
        );
    }

    @Test
    void registerUser_Success_UserDoNoExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(encryptionService.encrypt(anyString())).thenReturn("encryptedPassword");
        when(userRepository.saveAndFlush(any())).thenReturn(pendingActivationUser);

        var response = userService.registerUser(userRequest);

        verify(userProducer, times(1)).produceRegisterUserEvent(any());

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
        when(userRepository.saveAndFlush(any())).thenReturn(pendingActivationUser);

        var response = userService.registerUser(userRequest);

        verify(userProducer, times(1)).produceRegisterUserEvent(any());

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
    void registerUser_Fail_UserAlreadyExistsPendingActivation() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(pendingActivationUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(userRequest));
    }

    @Test
    void registerUser_Fail_UserAlreadyExistsPendingDeletion() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(pendingDeletionUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(userRequest));
    }

    @Test
    void activateUser_Success_PendingActivation() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(pendingActivationUser));
        when(authenticationService.createUser(any(RegisterUserEvent.class))).thenReturn("authUserId");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

        userService.activateUser(registerUserEvent);

        verify(authenticationService, times(1)).createUser(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void activateUser_Success_FailedActivation() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(failedActivationUser));
        when(authenticationService.createUser(any(RegisterUserEvent.class))).thenReturn("authUserId");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

        userService.activateUser(registerUserEvent);

        verify(authenticationService, times(1)).createUser(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void activateUser_Success_Deleted() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(deletedUser));
        when(authenticationService.createUser(any(RegisterUserEvent.class))).thenReturn("authUserId");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

        userService.activateUser(registerUserEvent);

        verify(authenticationService, times(1)).createUser(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void activateUser_Fail_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        userService.activateUser(registerUserEvent);

        verify(authenticationService, never()).createUser(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void activateUser_Fail_AuthServiceCreateUserReturnsNull() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(pendingActivationUser));
        when(authenticationService.createUser(any())).thenReturn(null);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(pendingActivationUser);

        userService.activateUser(registerUserEvent);

        verify(authenticationService, times(1)).createUser(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void activateUser_Fail_UserAlreadyActive() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

        userService.activateUser(registerUserEvent);

        verify(authenticationService, never()).createUser(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.email().toLowerCase())).thenReturn(Optional.of(user));
        when(authenticationService.authenticate(loginRequest)).thenReturn(authResponse);

        var response = userService.login(loginRequest);

        verify(userValidationService, times(1)).validateUser(anyString(), anyString());

        assertNotNull(response);
        assertEquals(authResponse.accessToken(), response.accessToken());
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

        verify(userValidationService).validateUserIdAndEmail(user.getId(), user.getEmail());

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
    }

    @Test
    void getUser_Success_WithNullEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), null)).thenReturn(List.of(user));

        var response = userService.getUser(user.getId(), null);

        verify(userValidationService).validateUserIdAndEmail(user.getId(), null);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
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

        verify(userMapper).updateEntityFromUserRequest(userUpdateRequest, user);

        assertNotNull(response);
        assertEquals(userUpdateRequest.firstName(), response.firstName());
        assertEquals(userUpdateRequest.lastName(), response.lastName());
    }

    @Test
    void updateUser_Success_WithNullEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), null)).thenReturn(List.of(user));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.updateUser(user.getId(), null, userUpdateRequest);

        verify(userMapper).updateEntityFromUserRequest(userUpdateRequest, user);

        assertNotNull(response);
        assertEquals(userUpdateRequest.firstName(), response.firstName());
        assertEquals(userUpdateRequest.lastName(), response.lastName());
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

        verify(userProducer, times(1)).produceDeleteUserEvent(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteUser_Success_WithNullEmail() {
        when(userRepository.findByIdOrEmail(user.getId(), null)).thenReturn(List.of(user));
        when(userRepository.saveAndFlush(any())).thenReturn(pendingDeletionUser);

        userService.deleteUser(user.getId(), null);

        verify(userProducer, times(1)).produceDeleteUserEvent(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteUser_Fail_UserNotFound() {
        when(userRepository.findByIdOrEmail(any(), anyString())).thenReturn(Collections.emptyList());
        doThrow(UserNotFoundException.class)
                .when(userValidationService)
                .validateUsers(anyLong(), anyString(), any());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(user.getId(), user.getEmail()));
    }

    @Test
    void deactivateUser_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(pendingDeletionUser));
        when(userRepository.saveAndFlush(any())).thenReturn(deletedUser);

        userService.deactivateUser(deleteUserEvent);

        verify(authenticationService, times(1)).deleteUser(any());
        verify(userRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void deactivateUser_Fail_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        userService.deactivateUser(deleteUserEvent);

        verify(authenticationService, never()).deleteUser(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void deactivateUser_Fail_UserNotPendingDeletion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.deactivateUser(deleteUserEvent);

        verify(authenticationService, never()).deleteUser(any());
        verify(userRepository, never()).saveAndFlush(any());
    }
}
