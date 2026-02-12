package uk.ac.soton.comp2300.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Setting {

    private static final Path REL_ASSETS_DIR = Paths.get("assets");
    private static final String SETTINGS_FILENAME = "settings.json";

    // Hard coded
    private static final String USERNAME = "Player1";
    private static final String PASSWORD = "Pass1";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static volatile boolean initialised = false;
    private static volatile boolean suppressAutoSave = false;

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
        PreferenceFile pf = new PreferenceFile();
        for (SettingOption opt : settingsList) {
            pf.settings.put(opt.getKey(), opt.isEnabled());
        }

        Path dir = resolveAssetsDir();
        Path file = dir.resolve(SETTINGS_FILENAME);
        try {
            Files.createDirectories(dir);
            try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(pf, w);
            }
        } catch (IOException e) {
            System.err.println("Failed to save settings to " + file + ": " + e.getMessage());
        }
    }

    public static synchronized void loadFromDisk() {
        Path dir = resolveAssetsDir();
        Path file = dir.resolve(SETTINGS_FILENAME);
        if (!Files.exists(file)) {
            saveToDisk();
            return;
        }

        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            PreferenceFile pf = GSON.fromJson(r, PreferenceFile.class);
            if (pf == null || pf.settings == null) return;
            applyMap(pf.settings);
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to load settings from " + file + ": " + e.getMessage());
        }
    }

    private static void applyMap(Map<String, Boolean> map) {
        for (SettingOption opt : settingsList) {
            Optional.ofNullable(map.get(opt.getKey())).ifPresent(opt::setEnabled);
        }
    }

    private static Path resolveAssetsDir() {
        return REL_ASSETS_DIR;
    }

    private static class PreferenceFile {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        Map<String, Boolean> settings = new HashMap<>();
    }

    public static class SettingOption {
        private final String key;
        private final String title;
        private final String description;

        private final BooleanProperty enabled;
        private final Consumer<Boolean> onChanged;

        public SettingOption(String key, String title, String description, boolean defaultValue, Consumer<Boolean> onChanged) {
            this.key = key;
            this.title = title;
            this.description = description;
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
        if (entered == null || entered.isBlank()) {
            return new PermissionResult(false, "Please enter your password.");
        }
        if (!PASSWORD.equals(entered)) {
            return new PermissionResult(false, "Password incorrect.");
        }
        return new PermissionResult(true, "Password correct.");
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


}
