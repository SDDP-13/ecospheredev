package uk.ac.soton.comp2300.model.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public class User {
    private String userName;
    private String storedHash;
    private String storedSalt;

    public User() {
        // Required for Gson.
    }

    public User(String userName, String rawPassword) {
        this.userName = normalizeUserName(userName);
        hashPassword(rawPassword);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = normalizeUserName(userName);
    }

    public String getStoredHash() {
        return storedHash;
    }

    public String getStoredSalt() {
        return storedSalt;
    }

    public void updatePassword(String rawPassword) {
        hashPassword(rawPassword);
    }

    public boolean verifyPassword(String rawPassword) {
        if (rawPassword == null || storedHash == null || storedSalt == null) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(storedSalt);
        String rawHash = generateHash(rawPassword, salt);
        return Objects.equals(storedHash, rawHash);
    }

    private void hashPassword(String rawPassword) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        hashPassword(rawPassword, salt);
    }

    private String hashPassword(String rawPassword, byte[] salt) {
        String hashedValue = generateHash(rawPassword, salt);
        this.storedHash = hashedValue;
        this.storedSalt = Base64.getEncoder().encodeToString(salt);
        return hashedValue;
    }

    private String generateHash(String rawPassword, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashedBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String normalizeUserName(String rawUserName) {
        return rawUserName == null ? "" : rawUserName.trim();
    }
}
