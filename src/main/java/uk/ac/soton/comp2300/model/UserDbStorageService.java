package uk.ac.soton.comp2300.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import uk.ac.soton.comp2300.model.user.UserDatabase;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserDbStorageService {

    private static final Path ASSETS_DIR = Paths.get("assets");
    private static final Path LEGACY_ASSERTS_DIR = Paths.get("asserts");
    private static final String DATABASE_FILENAME = "user_db.json";
    private static final String LEGACY_SETTINGS_FILENAME = "settings.json";
    private static final String PREVIOUS_DATABASE_FILENAME = "user_settings_db.json";
    private static final String LEGACY_COOKIE_FILENAME = "cookies.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public synchronized UserDbFile loadOrCreate(UserDbFile defaultFile) {
        migrateLegacySettingsFileIfNeeded();

        Path file = dbFilePath();
        if (!Files.exists(file)) {
            save(defaultFile);
            return copy(defaultFile);
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            UserDbFile loaded = GSON.fromJson(reader, UserDbFile.class);
            UserDbFile normalized = loaded == null ? copy(defaultFile) : normalize(loaded, defaultFile);
            boolean migratedCookies = migrateLegacyCookieDataIfNeeded(normalized);
            if (migratedCookies) {
                save(normalized);
            }
            return normalized;
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to load settings database from " + file + ": " + e.getMessage());
            return copy(defaultFile);
        }
    }

    public synchronized void save(UserDbFile data) {
        Path dir = ASSETS_DIR;
        Path file = dbFilePath();
        try {
            Files.createDirectories(dir);
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save settings database to " + file + ": " + e.getMessage());
        }
    }

    private Path dbFilePath() {
        return ASSETS_DIR.resolve(DATABASE_FILENAME);
    }

    private void migrateLegacySettingsFileIfNeeded() {
        Path legacyFile = ASSETS_DIR.resolve(LEGACY_SETTINGS_FILENAME);
        Path previousDatabaseFile = ASSETS_DIR.resolve(PREVIOUS_DATABASE_FILENAME);
        Path currentFile = dbFilePath();

        if (Files.exists(currentFile)) {
            return;
        }

        try {
            Files.createDirectories(ASSETS_DIR);
            if (Files.exists(previousDatabaseFile)) {
                Files.move(previousDatabaseFile, currentFile);
                return;
            }
            if (Files.exists(legacyFile)) {
                Files.move(legacyFile, currentFile);
            }
        } catch (IOException e) {
            System.err.println("Failed to migrate settings database to " + currentFile + ": " + e.getMessage());
        }
    }

    private boolean migrateLegacyCookieDataIfNeeded(UserDbFile target) {
        Path assetCookieFile = ASSETS_DIR.resolve(LEGACY_COOKIE_FILENAME);
        Path assertsCookieFile = LEGACY_ASSERTS_DIR.resolve(LEGACY_COOKIE_FILENAME);
        Path source = Files.exists(assetCookieFile) ? assetCookieFile : assertsCookieFile;

        if (!Files.exists(source)) {
            return false;
        }

        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            LegacyCookieJar legacy = GSON.fromJson(reader, LegacyCookieJar.class);
            if (legacy != null) {
                mergeLegacyCookieJar(target.cookieData, legacy);
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to migrate legacy cookies from " + source + ": " + e.getMessage());
            return false;
        }

        try {
            Files.deleteIfExists(source);
            if (!source.equals(assetCookieFile)) {
                Files.deleteIfExists(assetCookieFile);
            }
        } catch (IOException e) {
            System.err.println("Failed to remove legacy cookie file " + source + ": " + e.getMessage());
        }
        return true;
    }

    private void mergeLegacyCookieJar(CookieData cookieData, LegacyCookieJar legacy) {
        if (legacy.loggedInUser != null && !legacy.loggedInUser.isBlank()) {
            cookieData.loggedInUser = legacy.loggedInUser;
        }
        if (legacy.lastLoginDate != null && !legacy.lastLoginDate.isBlank()) {
            cookieData.lastLoginDate = legacy.lastLoginDate;
        }

        if (legacy.claimedTasksByUser != null && !legacy.claimedTasksByUser.isEmpty()) {
            legacy.claimedTasksByUser.forEach((userName, claimsByDate) ->
                    cookieData.claimedTasksByUser.put(userName, CookieData.copyClaimsByDate(claimsByDate)));
        } else if (legacy.claimedTasksByDate != null && legacy.loggedInUser != null && !legacy.loggedInUser.isBlank()) {
            cookieData.claimedTasksByUser.putIfAbsent(
                    legacy.loggedInUser,
                    CookieData.copyClaimsByDate(legacy.claimedTasksByDate)
            );
        }
    }

    private UserDbFile normalize(UserDbFile loaded, UserDbFile defaults) {
        UserDbFile normalized = new UserDbFile();
        normalized.settings.putAll(defaults.settings);
        normalized.settings.putAll(loaded.settings);
        normalized.userDatabase = loaded.userDatabase != null ? loaded.userDatabase : defaults.userDatabase;
        normalized.userSettings.putAll(defaults.userSettings);
        loaded.userSettings.forEach((user, value) -> normalized.userSettings.put(user, value));
        normalized.cookieData = CookieData.copyOf(loaded.cookieData != null ? loaded.cookieData : defaults.cookieData);
        return normalized;
    }

    private UserDbFile copy(UserDbFile source) {
        UserDbFile copy = new UserDbFile();
        copy.settings.putAll(source.settings);
        copy.userDatabase = source.userDatabase;
        source.userSettings.forEach((user, settings) -> copy.userSettings.put(user, settings));
        copy.cookieData = CookieData.copyOf(source.cookieData);
        return copy;
    }

    public static class UserDbFile {
        java.util.Map<String, Boolean> settings = new java.util.HashMap<>();
        UserDatabase userDatabase;
        java.util.Map<String, java.util.Map<String, Boolean>> userSettings = new java.util.HashMap<>();
        CookieData cookieData = new CookieData();
    }

    public static class CookieData {
        String loggedInUser;
        String lastLoginDate;
        java.util.Map<String, java.util.Map<String, java.util.Set<String>>> claimedTasksByUser = new java.util.HashMap<>();

        static CookieData copyOf(CookieData source) {
            CookieData normalized = new CookieData();
            if (source == null) {
                return normalized;
            }

            normalized.loggedInUser = source.loggedInUser;
            normalized.lastLoginDate = source.lastLoginDate;
            if (source.claimedTasksByUser != null) {
                source.claimedTasksByUser.forEach((userName, claimsByDate) ->
                        normalized.claimedTasksByUser.put(userName, copyClaimsByDate(claimsByDate)));
            }
            return normalized;
        }

        static java.util.Map<String, java.util.Set<String>> copyClaimsByDate(
                java.util.Map<String, java.util.Set<String>> claimsByDate
        ) {
            java.util.Map<String, java.util.Set<String>> copy = new java.util.HashMap<>();
            if (claimsByDate == null) {
                return copy;
            }

            claimsByDate.forEach((date, tasks) ->
                    copy.put(date, tasks == null ? new java.util.HashSet<>() : new java.util.HashSet<>(tasks)));
            return copy;
        }
    }

    private static class LegacyCookieJar {
        String loggedInUser;
        String lastLoginDate;
        java.util.Map<String, java.util.Map<String, java.util.Set<String>>> claimedTasksByUser = new java.util.HashMap<>();
        java.util.Map<String, java.util.Set<String>> claimedTasksByDate = new java.util.HashMap<>();
    }
}
