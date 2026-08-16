package com.yurupari.user_service.controller.v1;

import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.request.UserUpdateRequest;
import com.yurupari.user_service.model.http.response.UserResponse;
import com.yurupari.user_service.service.UserService;
import com.yurupari.user_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerV1Test {

    @InjectMocks
    private UserControllerV1 userControllerV1;

    @Mock
    private UserService userService;

    private UserRequest userRequest;
    private UserResponse userResponse;
    private UserUpdateRequest userUpdateRequest;

    @BeforeEach
    void setUp() {
        userRequest = TestModelFactory.createUserRequest(
                "test@email.com",
                "testPassword",
                "testName",
                "testLastName"
        );

        userResponse = TestModelFactory.createUserResponse(
                1L,
                "test@email.com",
                "testName",
                "testLastName"
        );

        userUpdateRequest = TestModelFactory.createUserUpdateRequest(
                "testPassword",
                "testName",
                "testLastName"
        );
    }

    @Test
    void registerUser_Success() {
        when(userService.registerUser(any())).thenReturn(userResponse);

        var response = userControllerV1.registerUser(userRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
    }

    @Test
    void registerUser_Fail_UserAlreadyExists() {
        when(userService.registerUser(any())).thenThrow(new UserAlreadyExistsException("test@email.com"));

        assertThrows(UserAlreadyExistsException.class, () -> userControllerV1.registerUser(userRequest));
    }

    @Test
    void getUser_Success() {
        when(userService.getUser(anyLong(), anyString())).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userControllerV1.getUser(1L, "test@email.com");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
    }

    @Test
    void getUser_Fail_UserNotFound() {
        when(userService.getUser(anyLong(), anyString())).thenThrow(new UserNotFoundException(1L, "test@email.com"));

        assertThrows(UserNotFoundException.class, () -> userControllerV1.getUser(1L, "test@email.com"));
    }

    @Test
    void updateUser_Success() {
        when(userService.updateUser(anyLong(), anyString(), any())).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userControllerV1.updateUser(1L, "test@email.com", userUpdateRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
    }

    @Test
    void updateUser_Fail_UserNotFound() {
        when(userService.updateUser(anyLong(), anyString(), any())).thenThrow(new UserNotFoundException(1L, "test@email.com"));

        assertThrows(UserNotFoundException.class, () -> userControllerV1.updateUser(1L, "test@email.com", userUpdateRequest));
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userService).deleteUser(anyLong(), anyString());

        ResponseEntity<Void> response = userControllerV1.deleteUser(1L, "test@email.com");

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteUser_Fail_UserNotFound() {
        doThrow(new UserNotFoundException(1L, "test@email.com")).when(userService).deleteUser(anyLong(), anyString());

        assertThrows(UserNotFoundException.class, () -> userControllerV1.deleteUser(1L, "test@email.com"));
    }
}
