package uk.ac.soton.comp2300.model.game_logic;
import com.google.gson.Gson;
import java.nio.file.*;
import java.io.IOException;

public class GameLoadManager {

    private Path saveFile;
    private Gson gson = new Gson();

    public GameLoadManager() {
        this(null);
    }

    public GameLoadManager(String userId) {
        Path baseDir = getBaseSaveDir();
        if (userId == null || userId.isBlank()) {
            saveFile = baseDir.resolve("player_save.json");
        } else {
            saveFile = baseDir.resolve("saves").resolve(sanitizeUserId(userId)).resolve("player_save.json");
        }
    }

    private Path getBaseSaveDir() {
        String override = System.getProperty("ecosphere.save.dir");
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        return Paths.get(System.getProperty("user.home"), ".ecospheredata");
    }

    private String sanitizeUserId(String userId) {
        return userId.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public GameState loadGame() {
        if (!Files.exists(saveFile)) {
            System.out.println("No game save found, starting new game.");
            return null;
        }

        try {
            String json = Files.readString(saveFile);
            GameState state = gson.fromJson(json, GameState.class);
            System.out.println("Loaded game: " + saveFile);
            return state;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
