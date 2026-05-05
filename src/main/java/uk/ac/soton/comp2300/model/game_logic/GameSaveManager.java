package uk.ac.soton.comp2300.model.game_logic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.soton.comp2300.model.ScheduleManager;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class GameSaveManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path saveFile;

    public GameSaveManager() {
        this(null);
    }

    public GameSaveManager(String userId) {
        saveFile = getSaveFilePath(userId);
        try {
            Files.createDirectories(saveFile.getParent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Path getSaveFilePath(String userId) {
        Path baseDir = getBaseSaveDir();
        if (userId == null || userId.isBlank()) {
            return baseDir.resolve("player_save.json");
        }
        return baseDir.resolve("saves").resolve(sanitizeUserId(userId)).resolve("player_save.json");
    }

    private static Path getBaseSaveDir() {
        String override = System.getProperty("ecosphere.save.dir");
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        return Paths.get(System.getProperty("user.home"), ".ecospheredata");
    }

    private String sanitizeUserId(String userId) {
        return userId.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static void deleteSaveForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        Path saveDir = getBaseSaveDir().resolve("saves").resolve(userId.replaceAll("[^a-zA-Z0-9._-]", "_"));
        if (!Files.exists(saveDir)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(saveDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete save path " + path + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Failed to delete save folder " + saveDir + ": " + e.getMessage());
        }
    }

    public void saveGame(GameState state) {
        try {
            state.setScheduleTasks(ScheduleManager.export());
            Files.createDirectories(saveFile.getParent());
            String json = gson.toJson(state);
            Files.writeString(saveFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved game: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

