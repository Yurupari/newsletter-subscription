package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.exception.AuthenticationException;
import com.yurupari.user_service.exception.MultipleUsersException;
import com.yurupari.user_service.exception.UserNotFoundException;
import com.yurupari.user_service.model.entity.User;
import com.yurupari.user_service.service.EncryptionService;
import com.yurupari.user_service.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserValidationServiceImpl implements UserValidationService {

    private final EncryptionService encryptionService;

    @Override
    public void validateUser(String encryptedPassword, String password) {
        if (encryptedPassword == null) {
            throw new IllegalArgumentException("Password from DB is null");
        }

        if (password == null) {
            throw new IllegalArgumentException("Password to validate is null");
        }

        var decryptedPassword = encryptionService.decrypt(encryptedPassword);

        if (!decryptedPassword.equals(password)) {
            throw new AuthenticationException("Passwords do not match");
        }
    }

    @Override
    public void validateUserIdAndEmail(Long id, String email) {
        if (id == null && email == null) {
            throw new IllegalArgumentException("Either id or email must be provided");
        }
    }

    @Override
    public void validateUsers(Long id, String email,List<User> users) {
        if (users.isEmpty()) {
            throw new UserNotFoundException(id, email);
        }

        if (users.size() > 1) {
            throw new MultipleUsersException(id, email);
        }
    }
}
