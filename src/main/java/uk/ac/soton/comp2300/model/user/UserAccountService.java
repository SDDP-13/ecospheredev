package uk.ac.soton.comp2300.model.user;

import java.util.Optional;

public class UserAccountService {

    private final UserDatabase userDatabase;

    public UserAccountService(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    public AuthResult login(String username, String password) {
        if (username == null || username.isBlank()) {
            return new AuthResult(false, "Please enter your username.", null, null);
        }
        if (password == null || password.isBlank()) {
            return new AuthResult(false, "Please enter your password.", null, null);
        }

        String normalizedUserName = normalizeUserName(username);
        Optional<User> user = userDatabase.findUserByUserName(normalizedUserName);
        if (user.isEmpty()) {
            return new AuthResult(false, "Username not found.", null, null);
        }
        if (!user.get().verifyPassword(password.trim())) {
            return new AuthResult(false, "Password incorrect.", null, null);
        }

        userDatabase.setCurrentUserName(user.get().getUserName());
        return new AuthResult(true, "Login successful.", user.get().getUserName(), user.get().getId());
    }

    public RegistrationResult register(String username, String password, String confirmPassword) {
        if (username == null || username.isBlank()) {
            return new RegistrationResult(false, "Please enter a username.", null, null);
        }
        if (password == null || password.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            return new RegistrationResult(false, "Please fill all account fields.", null, null);
        }

        String normalizedUserName = normalizeUserName(username);
        if (normalizedUserName.length() < 3) {
            return new RegistrationResult(false, "Username must be at least 3 characters.", null, null);
        }
        if (userDatabase.isUserNameTaken(normalizedUserName)) {
            return new RegistrationResult(false, "That username already exists.", null, null);
        }
        if (!password.equals(confirmPassword)) {
            return new RegistrationResult(false, "Passwords do not match.", null, null);
        }
        if (password.trim().length() < 4) {
            return new RegistrationResult(false, "Password must be at least 4 characters.", null, null);
        }

        User user = new User(normalizedUserName, password.trim());
        userDatabase.getUsers().add(user);
        userDatabase.setCurrentUserName(user.getUserName());
        return new RegistrationResult(true, "Account created.", user.getUserName(), user.getId());
    }

    public UsernameChangeResult updateUsername(String username) {
        User currentUser = userDatabase.getCurrentUser().orElse(null);
        if (currentUser == null) {
            return new UsernameChangeResult(false, "No active user found.", null);
        }
        if (username == null || username.isBlank()) {
            return new UsernameChangeResult(false, "Please enter a username.", null);
        }

        String normalizedUserName = normalizeUserName(username);
        if (normalizedUserName.equals(currentUser.getUserName())) {
            return new UsernameChangeResult(false, "New username must be different.", null);
        }

        Optional<User> duplicate = userDatabase.findUserByUserName(normalizedUserName);
        if (duplicate.isPresent() && duplicate.get() != currentUser) {
            return new UsernameChangeResult(false, "That username already exists.", null);
        }

        currentUser.setUserName(normalizedUserName);
        userDatabase.setCurrentUserName(normalizedUserName);
        return new UsernameChangeResult(true, "Username updated.", normalizedUserName);
    }

    public PasswordChangeResult updatePassword(String password, String confirmPassword) {
        User currentUser = userDatabase.getCurrentUser().orElse(null);
        if (currentUser == null) {
            return new PasswordChangeResult(false, "No active user found.", null);
        }
        if (password == null) password = "";
        if (confirmPassword == null) confirmPassword = "";
        if (password.isBlank() || confirmPassword.isBlank()) {
            return new PasswordChangeResult(false, "Please fill both password fields.", null);
        }
        if (!password.equals(confirmPassword)) {
            return new PasswordChangeResult(false, "Passwords do not match.", null);
        }

        String normalizedPassword = password.trim();
        if (normalizedPassword.length() < 4) {
            return new PasswordChangeResult(false, "Password must be at least 4 characters.", null);
        }

        currentUser.updatePassword(normalizedPassword);
        return new PasswordChangeResult(true, "Password updated.", normalizedPassword);
    }

    public DeleteAccountResult deleteCurrentUser() {
        User currentUser = userDatabase.getCurrentUser().orElse(null);
        if (currentUser == null) {
            return new DeleteAccountResult(false, "No active user found.", null);
        }

        String deletedUsername = currentUser.getUserName();
        boolean removed = userDatabase.getUsers().removeIf(user ->
                user.getUserName() != null && user.getUserName().equalsIgnoreCase(deletedUsername));

        if (!removed) {
            return new DeleteAccountResult(false, "Failed to delete account.", null);
        }

        String nextCurrentUser = userDatabase.getUsers().isEmpty()
                ? null
                : userDatabase.getUsers().get(0).getUserName();
        userDatabase.setCurrentUserName(nextCurrentUser);
        return new DeleteAccountResult(true, "Account deleted.", deletedUsername);
    }

    public record AuthResult(boolean ok, String msg, String username, String userId) { }

    public record RegistrationResult(boolean ok, String msg, String username, String userId) { }

    public record UsernameChangeResult(boolean ok, String msg, String username) { }

    public record PasswordChangeResult(boolean ok, String msg, String newPassword) { }

    public record DeleteAccountResult(boolean ok, String msg, String deletedUsername) { }

    private String normalizeUserName(String username) {
        return username == null ? "" : username.trim();
    }
}
