package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.exception.AuthenticationException;
import com.yurupari.user_service.exception.MultipleUsersException;
import com.yurupari.user_service.exception.UserNotFoundException;
import com.yurupari.user_service.model.entity.User;
import com.yurupari.user_service.service.EncryptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidationServiceImplTest {

    @InjectMocks
    private UserValidationServiceImpl userValidationService;

    @Mock
    private EncryptionService encryptionService;

    @Test
    void validateUser_Success() {
        var encryptedPassword = "encryptedPassword";
        var correctPassword = "password";

        when(encryptionService.decrypt(encryptedPassword)).thenReturn(correctPassword);

        assertDoesNotThrow(() -> userValidationService.validateUser(encryptedPassword, correctPassword));
    }

    @Test
    void validateUser_MismatchedPasswords() {
        var encryptedPassword = "encryptedPassword";
        var correctPassword = "password";
        var wrongPassword = "wrongPassword";

        when(encryptionService.decrypt(encryptedPassword)).thenReturn(correctPassword);

        assertThrows(AuthenticationException.class,
                () -> userValidationService.validateUser(encryptedPassword, wrongPassword));
    }

    @Test
    void validateUser_NullEncryptedPassword() {
        var correctPassword = "password";

        assertThrows(IllegalArgumentException.class,
                () -> userValidationService.validateUser(null, correctPassword));
    }

    @Test
    void validateUser_NullPassword() {
        var encryptedPassword = "encryptedPassword";

        assertThrows(IllegalArgumentException.class,
                () -> userValidationService.validateUser(encryptedPassword, null));
    }

    @Test
    void validateUserIdAndEmail_WithIdAndEmail() {
        assertDoesNotThrow(() -> userValidationService.validateUserIdAndEmail(1L, "test@example.com"));
    }

    @Test
    void validateUserIdAndEmail_WithIdOnly() {
        assertDoesNotThrow(() -> userValidationService.validateUserIdAndEmail(1L, null));
    }

    @Test
    void validateUserIdAndEmail_WithEmailOnly() {
        assertDoesNotThrow(() -> userValidationService.validateUserIdAndEmail(null, "test@example.com"));
    }

    @Test
    void validateUserIdAndEmail_WithNullIdAndNullEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> userValidationService.validateUserIdAndEmail(null, null));
    }

    @Test
    void validateUsers_Success_WithSingleUser() {
        List<User> users = List.of(new User());
        assertDoesNotThrow(() -> userValidationService.validateUsers(1L, "test@example.com", users));
    }

    @Test
    void validateUsers_Fail_WithEmptyList() {
        List<User> users = Collections.emptyList();
        assertThrows(UserNotFoundException.class,
                () -> userValidationService.validateUsers(1L, "test@example.com", users));
    }

    @Test
    void validateUsers_Fail_WithMultipleUsers() {
        List<User> users = List.of(new User(), new User());
        assertThrows(MultipleUsersException.class,
                () -> userValidationService.validateUsers(1L, "test@example.com", users));
    }
}
