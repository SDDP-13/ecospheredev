package uk.ac.soton.comp2300.model.game_logic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.*;

public class GameSaveManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path saveFile;

    public GameSaveManager() {
        saveFile = getSaveFilePath();
        try {
            Files.createDirectories(saveFile.getParent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Path getSaveFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".ecospheredata", "player_save.json");
    }

    public void saveGame(GameState state) {
        try {
            String json = gson.toJson(state);
            Files.writeString(saveFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved game: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

