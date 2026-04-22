package uk.ac.soton.comp2300.model.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDatabase {

    private List<User> users = new ArrayList<>();
    private String currentUserName;

    public List<User> getUsers() {
        return users;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public Optional<User> findUserByUserName(String userName) {
        return users.stream()
                .filter(user -> user.getUserName() != null)
                .filter(user -> user.getUserName().equalsIgnoreCase(userName))
                .findFirst();
    }

    public Optional<User> getCurrentUser() {
        if (currentUserName == null || currentUserName.isBlank()) {
            return Optional.empty();
        }
        return findUserByUserName(currentUserName);
    }
}
