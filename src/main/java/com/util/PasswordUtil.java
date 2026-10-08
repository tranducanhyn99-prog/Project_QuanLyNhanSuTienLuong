package com.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final int ITERATIONS = 600000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordUtil() { }
    public static String hashPassword(String password) {
        byte[] salt = new byte[16]; RANDOM.nextBytes(salt);
        return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }
    public static boolean verifyPassword(String password, String stored) {
        if (password == null || stored == null) return false;
        if (stored.matches("[0-9a-fA-F]{64}")) {
            return MessageDigest.isEqual(hashSHA256(password).getBytes(StandardCharsets.US_ASCII),
                    stored.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
        }
        try {
            String[] fields = stored.split("\\$", -1);
            if (fields.length != 4 || !"pbkdf2-sha256".equals(fields[0])) return false;
            int iterations = Integer.parseInt(fields[1]);
            if (iterations < 100000 || iterations > 2000000) return false;
            byte[] salt = Base64.getDecoder().decode(fields[2]);
            byte[] hash = Base64.getDecoder().decode(fields[3]);
            return salt.length == 16 && hash.length == 32
                    && MessageDigest.isEqual(hash, derive(password, salt, iterations));
        } catch (IllegalArgumentException ex) { return false; }
    }
    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (java.security.GeneralSecurityException ex) { throw new IllegalStateException(ex); }
        finally { spec.clearPassword(); }
    }
    /** Legacy verification/fixtures only; new accounts use hashPassword. */
    public static String hashSHA256(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte value : digest) hex.append(String.format("%02x", value & 255));
            return hex.toString();
        } catch (java.security.GeneralSecurityException ex) { throw new IllegalStateException(ex); }
    }
}
