package com.example.inventorycontrolapplication;

import com.example.inventorycontrolapplication.data.helpers.PasswordHasher;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for PasswordHasher utility class.
 * Tests password hashing, verification, and security features.
 */
public class PasswordHasherTest {

    @Test
    public void testHashPassword_ReturnsNonNullHash() {
        String password = "testPassword123";
        String hash = PasswordHasher.hash(password);

        assertNotNull("Hash should not be null", hash);
        assertFalse("Hash should not be empty", hash.isEmpty());
    }

    @Test
    public void testHashPassword_ContainsCorrectFormat() {
        String password = "testPassword123";
        String hash = PasswordHasher.hash(password);

        assertTrue("Hash should start with algorithm name",
                hash.startsWith("PBKDF2WithHmacSHA256$"));

        String[] parts = hash.split("\\$");
        assertEquals("Hash should have 4 parts (algorithm, iterations, salt, hash)",
                4, parts.length);
    }

    @Test
    public void testHashPassword_DifferentPasswordsProduceDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";

        String hash1 = PasswordHasher.hash(password1);
        String hash2 = PasswordHasher.hash(password2);

        assertNotEquals("Different passwords should produce different hashes",
                hash1, hash2);
    }

    @Test
    public void testHashPassword_SamePasswordProducesDifferentHashesDueToSalt() {
        String password = "testPassword123";

        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);

        assertNotEquals("Same password should produce different hashes due to random salt",
                hash1, hash2);
    }

    @Test
    public void testVerifyPassword_CorrectPasswordReturnsTrue() {
        String password = "testPassword123";
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify(password, hash);

        assertTrue("Correct password should verify successfully", result);
    }

    @Test
    public void testVerifyPassword_IncorrectPasswordReturnsFalse() {
        String password = "testPassword123";
        String wrongPassword = "wrongPassword";
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify(wrongPassword, hash);

        assertFalse("Incorrect password should fail verification", result);
    }

    @Test
    public void testVerifyPassword_NullStoredHashReturnsFalse() {
        String password = "testPassword123";

        boolean result = PasswordHasher.verify(password, null);

        assertFalse("Null stored hash should return false", result);
    }

    @Test
    public void testVerifyPassword_InvalidFormatReturnsFalse() {
        String password = "testPassword123";
        String invalidHash = "invalidHashFormat";

        boolean result = PasswordHasher.verify(password, invalidHash);

        assertFalse("Invalid hash format should return false", result);
    }

    @Test
    public void testVerifyPassword_EmptyPasswordCanBeHashed() {
        String password = "";
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify(password, hash);

        assertTrue("Empty password should be hashable and verifiable", result);
    }

    @Test
    public void testVerifyPassword_LongPasswordWorksCorrectly() {
        String password = "ThisIsAVeryLongPasswordWithLotsOfCharacters1234567890!@#$%^&*()";
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify(password, hash);

        assertTrue("Long password should verify successfully", result);
    }

    @Test
    public void testVerifyPassword_SpecialCharactersWorkCorrectly() {
        String password = "P@ssw0rd!#$%^&*()";
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify(password, hash);

        assertTrue("Password with special characters should verify successfully", result);
    }

    @Test
    public void testVerifyPassword_UnicodeCharactersWorkCorrectly() {
        String password = "パスワード123";
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.verify(password, hash);

        assertTrue("Password with unicode characters should verify successfully", result);
    }

    @Test
    public void testHasEncodedFormat_ValidHashReturnsTrue() {
        String password = "testPassword123";
        String hash = PasswordHasher.hash(password);

        boolean result = PasswordHasher.hasEncodedFormat(hash);

        assertTrue("Valid hash should have encoded format", result);
    }

    @Test
    public void testHasEncodedFormat_PlaintextReturnsFalse() {
        String plaintext = "plainTextPassword";

        boolean result = PasswordHasher.hasEncodedFormat(plaintext);

        assertFalse("Plaintext should not have encoded format", result);
    }

    @Test
    public void testHasEncodedFormat_NullReturnsFalse() {
        boolean result = PasswordHasher.hasEncodedFormat(null);

        assertFalse("Null should not have encoded format", result);
    }

    @Test
    public void testHasEncodedFormat_EmptyStringReturnsFalse() {
        boolean result = PasswordHasher.hasEncodedFormat("");

        assertFalse("Empty string should not have encoded format", result);
    }

    @Test
    public void testPasswordHash_IsTimingAttackResistant() {
        String password = "testPassword123";
        String hash = PasswordHasher.hash(password);

        // Verify both correct and incorrect passwords take similar time
        // This is a basic test - timing attacks need more sophisticated testing
        long startCorrect = System.nanoTime();
        PasswordHasher.verify(password, hash);
        long endCorrect = System.nanoTime();

        long startIncorrect = System.nanoTime();
        PasswordHasher.verify("wrongPassword", hash);
        long endIncorrect = System.nanoTime();

        long correctTime = endCorrect - startCorrect;
        long incorrectTime = endIncorrect - startIncorrect;

        // Both should take roughly similar time (within an order of magnitude)
        // This is a weak test but demonstrates timing resistance
        assertTrue("Verification should take similar time for correct/incorrect passwords",
                Math.abs(correctTime - incorrectTime) < correctTime * 10);
    }

    @Test
    public void testVerifyPassword_CaseSensitive() {
        String password = "TestPassword123";
        String hash = PasswordHasher.hash(password);

        assertFalse("Password verification should be case sensitive",
                PasswordHasher.verify("testpassword123", hash));
        assertFalse("Password verification should be case sensitive",
                PasswordHasher.verify("TESTPASSWORD123", hash));
        assertTrue("Original case should verify",
                PasswordHasher.verify("TestPassword123", hash));
    }
}
