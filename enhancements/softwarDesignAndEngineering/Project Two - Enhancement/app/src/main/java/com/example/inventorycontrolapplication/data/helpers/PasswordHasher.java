package com.example.inventorycontrolapplication.data.helpers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import android.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 120000; // reasonable default
    private static final int KEY_LENGTH_BITS = 256; // 32 bytes
    private static final int SALT_LENGTH_BYTES = 16; // 128-bit salt

    private PasswordHasher() {}

    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] derived = pbkdf2(password.toCharArray(), salt, DEFAULT_ITERATIONS, KEY_LENGTH_BITS);
        String saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP);
        String hashB64 = Base64.encodeToString(derived, Base64.NO_WRAP);
        return String.format("%s$%d$%s$%s", ALGORITHM, DEFAULT_ITERATIONS, saltB64, hashB64);
    }

    public static boolean verify(String password, String stored) {
        if (stored == null) return false;
        String[] parts = stored.split("\\$");
        if (parts.length != 4) return false;
        String alg = parts[0];
        if (!ALGORITHM.equals(alg)) return false;
        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        byte[] salt = Base64.decode(parts[2], Base64.DEFAULT);
        byte[] expected = Base64.decode(parts[3], Base64.DEFAULT);
        byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
        return constantTimeEquals(expected, actual);
    }

    public static boolean hasEncodedFormat(String stored) {
        return stored != null && stored.startsWith(ALGORITHM + "$");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 failure", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
