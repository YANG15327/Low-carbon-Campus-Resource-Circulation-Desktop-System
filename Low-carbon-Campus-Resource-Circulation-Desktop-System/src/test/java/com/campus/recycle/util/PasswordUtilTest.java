package com.campus.recycle.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashWithSameSaltIsDeterministic() {
        String salt = PasswordUtil.generateSalt();
        String hash1 = PasswordUtil.hash("secret123", salt);
        String hash2 = PasswordUtil.hash("secret123", salt);
        assertTrue(PasswordUtil.verify("secret123", salt, hash1));
        assertTrue(PasswordUtil.verify("secret123", salt, hash2));
    }

    @Test
    void hashWithDifferentSaltDiffers() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        assertNotEquals(
                PasswordUtil.hash("secret123", salt1),
                PasswordUtil.hash("secret123", salt2));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("secret123", salt);
        assertFalse(PasswordUtil.verify("wrong123", salt, hash));
    }

    @Test
    void generatedSaltIsUniqueEnough() {
        assertNotEquals(PasswordUtil.generateSalt(), PasswordUtil.generateSalt());
    }
}
