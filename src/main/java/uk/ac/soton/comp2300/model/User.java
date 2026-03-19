package uk.ac.soton.comp2300.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class User {
    private String userName;
    private String storedHash;
    private String storedSalt;
    private String email;

    public User(String userName, String rawPassword) {
        this(userName, rawPassword, "");
    }

    public User(String userName, String rawPassword, String email) {
        this.userName = userName;
        hashPassword(rawPassword);
        this.email = email;
    }


    private void hashPassword(String rawPassword) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        hashPassword(rawPassword, salt);
    }

    private void hashPassword(String rawPassword, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashedBytes = digest.digest(rawPassword.getBytes());
            this.storedHash = Base64.getEncoder().encodeToString(hashedBytes);
            this.storedSalt = Base64.getEncoder().encodeToString(salt);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
