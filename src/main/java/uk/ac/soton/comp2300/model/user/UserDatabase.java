package uk.ac.soton.comp2300.model.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDatabase {

    private List<User> users = new ArrayList<>();
    private String currentUserName;

    public List<User> getUsers() {
        if (users == null) {
            users = new ArrayList<>();
        }
        users.forEach(User::ensureId);
        return users;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public Optional<User> findUserByUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            return Optional.empty();
        }

        String normalizedUserName = normalizeUserName(userName);
        return getUsers().stream()
                .filter(user -> user.getUserName() != null)
                .filter(user -> normalizeUserName(user.getUserName()).equals(normalizedUserName))
                .findFirst();
    }

    public boolean isUserNameTaken(String userName) {
        return findUserByUserName(userName).isPresent();
    }

    public Optional<User> getCurrentUser() {
        if (currentUserName == null || currentUserName.isBlank()) {
            return Optional.empty();
        }
        return findUserByUserName(currentUserName);
    }

    private String normalizeUserName(String userName) {
        return userName.trim().toLowerCase();
    }
}
