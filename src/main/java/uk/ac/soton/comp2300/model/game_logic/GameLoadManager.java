package uk.ac.soton.comp2300.model.game_logic;
import com.google.gson.Gson;
import java.nio.file.*;
import java.io.IOException;

public class GameLoadManager {

    private Path saveFile;
    private Gson gson = new Gson();

    public GameLoadManager() {
        String userHome = System.getProperty("user.home");
        saveFile = Paths.get(userHome, ".ecospheredata", "player_save.json");
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
