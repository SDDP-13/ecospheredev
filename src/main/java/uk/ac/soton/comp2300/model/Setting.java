package uk.ac.soton.comp2300.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import uk.ac.soton.comp2300.model.user.User;
import uk.ac.soton.comp2300.model.user.UserAccountService;
import uk.ac.soton.comp2300.model.user.UserDatabase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class Setting {

    private static final UserDbStorageService STORAGE = new UserDbStorageService();

    private static volatile boolean initialised = false;
    private static volatile boolean suppressAutoSave = false;
    private static UserDatabase userDatabase = createEmptyUserDatabase();
    private static Map<String, Map<String, Boolean>> userSettings = new HashMap<>();
    private static UserDbStorageService.CookieData cookieData = new UserDbStorageService.CookieData();

    public static final List<SettingOption> settingsList = List.of(
            new SettingOption("notifications", "Notifications", "Keep up to date while not in game", false, enabled -> {
                System.out.println(enabled ? "Notifications ON" : "Notifications OFF");
            }),
            new SettingOption("darkMode", "Dark mode", "For low light conditions", false, enabled -> {
                System.out.println("Dark mode = " + enabled);
            }),

            new SettingOption("shareUsageData", "Use of data", "Allow anonymous usage analytics to improve the game", false, enabled -> {
                System.out.println("Share usage data = " + enabled);
            }),
            new SettingOption("sendCrashReports", "Crash reports", "Automatically send crash reports to help fix bugs", true, enabled -> {
                System.out.println("Send crash reports = " + enabled);
            })
    );

    public static synchronized void init() {
        if (initialised) return;

        suppressAutoSave = true;
        loadFromDisk();
        suppressAutoSave = false;

        initialised = true;
    }

    public static synchronized void saveToDisk() {
        persistCurrentUserSettings();
        STORAGE.save(snapshotForStorage());
    }

    public static synchronized void loadFromDisk() {
        UserDbStorageService.UserDbFile loaded = STORAGE.loadOrCreate(buildDefaultStorageFile());
        if (loaded.userDatabase != null && !loaded.userDatabase.getUsers().isEmpty()) {
            userDatabase = loaded.userDatabase;
            if (userDatabase.getCurrentUser().isEmpty()) {
                userDatabase.setCurrentUserName(userDatabase.getUsers().get(0).getUserName());
            }
        } else {
            userDatabase = createEmptyUserDatabase();
        }

        userSettings = new HashMap<>();
        if (loaded.userSettings != null && !loaded.userSettings.isEmpty()) {
            loaded.userSettings.forEach((userName, settingsMap) ->
                    userSettings.put(userName, normalizeSettingsMap(settingsMap)));
        } else if (loaded.settings != null) {
            getCurrentUser().ifPresent(user ->
                    userSettings.put(user.getUserName(), normalizeSettingsMap(loaded.settings)));
        }

        cookieData = UserDbStorageService.CookieData.copyOf(loaded.cookieData);
        ensureCurrentUserSettingsExist();
        applySettingsForCurrentUser();
        cleanupCookieClaims();
    }

    private static void applyMap(Map<String, Boolean> map) {
        for (SettingOption opt : settingsList) {
            Optional.ofNullable(map.get(opt.getKey())).ifPresent(opt::setEnabled);
        }
    }

    private static void applySettingsForCurrentUser() {
        suppressAutoSave = true;
        getCurrentUser().ifPresent(user -> applyMap(getSettingsForUser(user.getUserName())));
        suppressAutoSave = false;
    }

    private static Map<String, Boolean> getSettingsForUser(String username) {
        return userSettings.computeIfAbsent(username, ignored -> defaultSettingsMap());
    }

    private static void ensureCurrentUserSettingsExist() {
        getCurrentUser().ifPresent(user ->
                userSettings.computeIfAbsent(user.getUserName(), ignored -> defaultSettingsMap()));
    }

    private static void persistCurrentUserSettings() {
        getCurrentUser().ifPresent(user ->
                userSettings.put(user.getUserName(), snapshotCurrentSettings()));
    }

    private static Map<String, Boolean> snapshotCurrentSettings() {
        Map<String, Boolean> snapshot = new HashMap<>();
        for (SettingOption opt : settingsList) {
            snapshot.put(opt.getKey(), opt.isEnabled());
        }
        return snapshot;
    }

    private static Map<String, Boolean> defaultSettingsMap() {
        Map<String, Boolean> defaults = new HashMap<>();
        for (SettingOption opt : settingsList) {
            defaults.put(opt.getKey(), opt.getDefaultValue());
        }
        return defaults;
    }

    private static Map<String, Boolean> normalizeSettingsMap(Map<String, Boolean> map) {
        Map<String, Boolean> normalized = defaultSettingsMap();
        if (map != null) {
            normalized.putAll(map);
        }
        return normalized;
    }

    private static Map<String, Map<String, Boolean>> deepCopyUserSettings() {
        Map<String, Map<String, Boolean>> copy = new HashMap<>();
        userSettings.forEach((userName, settingsMap) ->
                copy.put(userName, new HashMap<>(normalizeSettingsMap(settingsMap))));
        return copy;
    }

    private static void cleanupCookieClaims() {
        String today = java.time.LocalDate.now().toString();
        cookieData.claimedTasksByUser.values().forEach(claimsByDate ->
                claimsByDate.entrySet().removeIf(entry -> !today.equals(entry.getKey())));
    }

    private static UserDatabase createEmptyUserDatabase() {
        return new UserDatabase();
    }

    private static UserAccountService accountService() {
        return new UserAccountService(userDatabase);
    }

    private static Optional<User> getCurrentUser() {
        return userDatabase.getCurrentUser();
    }

    private static User requireCurrentUser() {
        return getCurrentUser().orElseThrow(() ->
                new IllegalStateException("No active user found."));
    }

    public static class SettingOption {
        private final String key;
        private final String title;
        private final String description;
        private final boolean defaultValue;

        private final BooleanProperty enabled;
        private final Consumer<Boolean> onChanged;

        public SettingOption(String key, String title, String description, boolean defaultValue, Consumer<Boolean> onChanged) {
            this.key = key;
            this.title = title;
            this.description = description;
            this.defaultValue = defaultValue;
            this.enabled = new SimpleBooleanProperty(defaultValue);
            this.onChanged = onChanged;

            this.enabled.addListener((obs, oldV, newV) -> {
                if (this.onChanged != null) this.onChanged.accept(newV);
                if (Setting.initialised && !Setting.suppressAutoSave) 
                Setting.saveToDisk();
            });
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }

        public String getKey() { return key; }
        public boolean getDefaultValue() { return defaultValue; }

        public boolean isEnabled() { return enabled.get(); }
        public void setEnabled(boolean value) { enabled.set(value); }
        public BooleanProperty enabledProperty() { return enabled; }
    }

    public static class PermissionResult {
        public final boolean ok;
        public final String msg;

        public PermissionResult(boolean ok, String msg) {
            this.ok = ok;
            this.msg = msg;
        }
    }

    public static PermissionResult checkPassword(String entered) {
        init();
        if (getCurrentUser().isEmpty()) {
            return new PermissionResult(false, "No active user found.");
        }
        if (entered == null || entered.isBlank()) {
            return new PermissionResult(false, "Please enter your password.");
        }
        if (!requireCurrentUser().verifyPassword(entered.trim())) {
            return new PermissionResult(false, "Password incorrect.");
        }
        return new PermissionResult(true, "Password correct.");
    }

    public static class LoginResult {
        public final boolean ok;
        public final String msg;
        public final String username;

        public LoginResult(boolean ok, String msg, String username) {
            this.ok = ok;
            this.msg = msg;
            this.username = username;
        }
    }

    public static synchronized String getUsername() {
        init();
        return getCurrentUser().map(User::getUserName).orElse("");
    }

    public static synchronized String getPasswordMask() {
        init();
        if (getCurrentUser().isEmpty()) {
            return "";
        }
        int length = 8;
        User currentUser = requireCurrentUser();
        if (currentUser.getStoredHash() != null) {
            length = Math.max(4, Math.min(12, currentUser.getStoredHash().length() / 6));
        }
        return "\u25CF".repeat(length);
    }

    public static synchronized LoginResult login(String username, String password) {
        init();
        UserAccountService.AuthResult result = accountService().login(username, password);
        if (result.ok()) {
            applySettingsForCurrentUser();
            recordCookieLogin(result.username());
        }
        return new LoginResult(result.ok(), result.msg(), result.username());
    }

    public static class RegisterResult {
        public final boolean ok;
        public final String msg;
        public final String username;

        public RegisterResult(boolean ok, String msg, String username) {
            this.ok = ok;
            this.msg = msg;
            this.username = username;
        }
    }

    public static synchronized RegisterResult createAccount(String username, String password, String confirmPassword) {
        init();
        UserAccountService.RegistrationResult result = accountService().register(username, password, confirmPassword);
        if (result.ok()) {
            userSettings.put(result.username(), defaultSettingsMap());
            applySettingsForCurrentUser();
            saveToDisk();
        }
        return new RegisterResult(result.ok(), result.msg(), result.username());
    }

    public static class UsernameResult {
        public final boolean ok;
        public final String msg;
        public final String username; // normalized (trimmed) username when ok, else null

        public UsernameResult(boolean ok, String msg, String username) {
            this.ok = ok;
            this.msg = msg;
            this.username = username;
        }
    }

    public static UsernameResult validateUsername(String entered, String currentUsername) {
        if (entered == null) {
            return new UsernameResult(false, "Please enter a username.", null);
        }

        String u = entered.trim();

        if (u.isEmpty()) {
            return new UsernameResult(false, "Please enter a username.", null);
        }
        if (currentUsername != null && u.equals(currentUsername)) {
            return new UsernameResult(false, "New username must be different.", null);
        }

        // change username fn here
        return new UsernameResult(true, "OK", u);
    }

    public static synchronized UsernameResult updateUsername(String entered) {
        init();
        if (getCurrentUser().isEmpty()) {
            return new UsernameResult(false, "No active user found.", null);
        }
        String previousUserName = requireCurrentUser().getUserName();
        UserAccountService.UsernameChangeResult result = accountService().updateUsername(entered);
        if (result.ok()) {
            Map<String, Boolean> previousSettings = userSettings.remove(previousUserName);
            userSettings.put(result.username(), normalizeSettingsMap(previousSettings));
            moveCookieDataToRenamedUser(previousUserName, result.username());
            applySettingsForCurrentUser();
            saveToDisk();
        }
        return new UsernameResult(result.ok(), result.msg(), result.username());
    }

    public static class PasswordChangeResult {
        public final boolean ok;
        public final String msg;
        public final String newPassword;

        public PasswordChangeResult(boolean ok, String msg, String newPassword) {
            this.ok = ok;
            this.msg = msg;
            this.newPassword = newPassword;
        }
    }

    public static PasswordChangeResult validateNewPassword(String p1, String p2) {
        if (p1 == null) p1 = "";
        if (p2 == null) p2 = "";

        if (p1.isBlank() || p2.isBlank()) {
            return new PasswordChangeResult(false, "Please fill both password fields.", null);
        }
        if (!p1.equals(p2)) {
            return new PasswordChangeResult(false, "Passwords do not match.", null);
        }

        String pw = p1.trim();
        if (pw.length() < 4) {
            return new PasswordChangeResult(false, "Password must be at least 4 characters.", null);
        }

        return new PasswordChangeResult(true, "OK", pw);
    }

    public static synchronized PasswordChangeResult updatePassword(String p1, String p2) {
        init();
        UserAccountService.PasswordChangeResult result = accountService().updatePassword(p1, p2);
        if (result.ok()) {
            saveToDisk();
        }
        return new PasswordChangeResult(result.ok(), result.msg(), result.newPassword());
    }

    public static class DeleteAccountResult {
        public final boolean ok;
        public final String msg;
        public final String deletedUsername;

        public DeleteAccountResult(boolean ok, String msg, String deletedUsername) {
            this.ok = ok;
            this.msg = msg;
            this.deletedUsername = deletedUsername;
        }
    }

    public static synchronized DeleteAccountResult deleteCurrentAccount() {
        init();
        if (getCurrentUser().isEmpty()) {
            return new DeleteAccountResult(false, "No active user found.", null);
        }
        String deletedUsername = requireCurrentUser().getUserName();
        UserAccountService.DeleteAccountResult result = accountService().deleteCurrentUser();
        if (!result.ok()) {
            return new DeleteAccountResult(false, result.msg(), result.deletedUsername());
        }

        userSettings.remove(deletedUsername);
        cookieData.claimedTasksByUser.remove(deletedUsername);
        if (deletedUsername.equals(cookieData.loggedInUser)) {
            cookieData.loggedInUser = null;
        }

        ensureCurrentUserSettingsExist();
        applySettingsForCurrentUser();
        saveToDisk();
        return new DeleteAccountResult(true, "Account deleted.", deletedUsername);
    }

    public static boolean isDarkMode(){
        for (SettingOption option: Setting.settingsList){
            if (option.getKey().equals("darkMode"))
                return option.enabledProperty().get();
        }
        return false;
    }

    public static synchronized void recordCookieLogin(String username) {
        init();
        if (username == null || username.isBlank()) {
            return;
        }
        cookieData.loggedInUser = username;
        cookieData.lastLoginDate = java.time.LocalDate.now().toString();
        saveToDisk();
    }

    public static synchronized void clearCookieLogin() {
        init();
        cookieData.loggedInUser = null;
        saveToDisk();
    }

    public static synchronized Optional<String> getCookieLoggedInUser() {
        init();
        return Optional.ofNullable(cookieData.loggedInUser);
    }

    public static synchronized Set<String> getClaimedTasksTodayForLoggedInUser() {
        init();
        String activeUser = cookieData.loggedInUser;
        if (activeUser == null || activeUser.isBlank()) {
            return Set.of();
        }

        cleanupCookieClaims();
        return new HashSet<>(getClaimsForUser(activeUser).getOrDefault(java.time.LocalDate.now().toString(), Set.of()));
    }

    public static synchronized boolean hasClaimedTaskTodayForLoggedInUser(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            return false;
        }
        return getClaimedTasksTodayForLoggedInUser().contains(taskId);
    }

    public static synchronized void markTaskClaimedTodayForLoggedInUser(String taskId) {
        init();
        if (taskId == null || taskId.isBlank()) {
            return;
        }

        String activeUser = cookieData.loggedInUser;
        if (activeUser == null || activeUser.isBlank()) {
            return;
        }

        cleanupCookieClaims();
        getClaimsForUser(activeUser)
                .computeIfAbsent(java.time.LocalDate.now().toString(), ignored -> new HashSet<>())
                .add(taskId);
        saveToDisk();
    }

    private static Map<String, Set<String>> getClaimsForUser(String username) {
        return cookieData.claimedTasksByUser.computeIfAbsent(username, ignored -> new HashMap<>());
    }

    private static void moveCookieDataToRenamedUser(String oldUsername, String newUsername) {
        if (oldUsername == null || newUsername == null || oldUsername.equals(newUsername)) {
            return;
        }

        Map<String, Set<String>> claims = cookieData.claimedTasksByUser.remove(oldUsername);
        if (claims != null) {
            cookieData.claimedTasksByUser.put(newUsername, claims);
        }
        if (oldUsername.equals(cookieData.loggedInUser)) {
            cookieData.loggedInUser = newUsername;
        }
    }

    private static UserDbStorageService.UserDbFile buildDefaultStorageFile() {
        UserDbStorageService.UserDbFile defaults = new UserDbStorageService.UserDbFile();
        defaults.userDatabase = createEmptyUserDatabase();
        defaults.cookieData = new UserDbStorageService.CookieData();
        return defaults;
    }

    private static UserDbStorageService.UserDbFile snapshotForStorage() {
        UserDbStorageService.UserDbFile snapshot = new UserDbStorageService.UserDbFile();
        snapshot.userDatabase = userDatabase;
        snapshot.userSettings = deepCopyUserSettings();
        snapshot.cookieData = UserDbStorageService.CookieData.copyOf(cookieData);
        return snapshot;
    }
}
