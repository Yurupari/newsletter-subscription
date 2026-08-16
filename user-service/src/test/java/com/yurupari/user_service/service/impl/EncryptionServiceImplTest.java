package com.yurupari.user_service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceImplTest {

    private EncryptionServiceImpl encryptionService;

    private static final String TEST_BASE64_KEY = "vX2R84p8aWn/oYf4zO4JpM3t1eU9qR5m0bV7cK6l1wE=";

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionServiceImpl(
                TEST_BASE64_KEY,
                "AES",
                "AES/GCM/NoPadding",
                128,
                12
        );
    }

    @Test
    void encryptAndDecrypt_Success() {
        var originalText = "Hello, Secure World! 123";

        var encryptedText = encryptionService.encrypt(originalText);
        var decryptedText = encryptionService.decrypt(encryptedText);

        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);
        assertEquals(originalText, decryptedText);
    }

    @Test
    void encrypt_ProducesDifferentCiphertextEachTime() {
        var originalText = "SamePlainText";

        var encrypted1 = encryptionService.encrypt(originalText);
        var encrypted2 = encryptionService.encrypt(originalText);

        assertNotEquals(encrypted1, encrypted2);
        assertEquals(originalText, encryptionService.decrypt(encrypted1));
        assertEquals(originalText, encryptionService.decrypt(encrypted2));
    }

    @Test
    void decrypt_ThrowsExceptionOnCorruptedData() {
        var invalidBase64 = "SomeCorruptedOrTamperedBase64DataString==";

        assertThrows(RuntimeException.class, () -> encryptionService.decrypt(invalidBase64));
    }

    @Test
    void constructor_ThrowsExceptionOnInvalidKey() {
        var invalidKey = "NotABase64String!!!";

        assertThrows(RuntimeException.class, () -> new EncryptionServiceImpl(
                invalidKey,
                "AES",
                "AES/GCM/NoPadding",
                128,
                12
        ));
    }
}