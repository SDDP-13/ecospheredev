package uk.ac.soton.comp2300.model;

import java.util.Optional;
import java.util.Set;

public class CookieStorageService {

    public synchronized void login(String username) {
        Setting.recordCookieLogin(username);
    }

    public synchronized void logout() {
        Setting.clearCookieLogin();
    }

    public synchronized Optional<String> getLoggedInUser() {
        return Setting.getCookieLoggedInUser();
    }

    public synchronized boolean hasClaimedTaskToday(String taskId) {
        return Setting.hasClaimedTaskTodayForLoggedInUser(taskId);
    }

    public synchronized Set<String> getClaimedTasksToday() {
        return Setting.getClaimedTasksTodayForLoggedInUser();
    }

    public synchronized void markTaskClaimedToday(String taskId) {
        Setting.markTaskClaimedTodayForLoggedInUser(taskId);
    }
}
