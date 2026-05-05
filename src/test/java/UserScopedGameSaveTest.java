package uk.ac.soton.comp2300;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.soton.comp2300.model.Resource;
import uk.ac.soton.comp2300.model.game_logic.GameController;
import uk.ac.soton.comp2300.model.game_logic.GameLoadManager;
import uk.ac.soton.comp2300.model.game_logic.GameSaveManager;
import uk.ac.soton.comp2300.model.game_logic.GameState;
import uk.ac.soton.comp2300.model.user.UserAccountService;
import uk.ac.soton.comp2300.model.user.UserDatabase;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserScopedGameSaveTest {

    @TempDir
    Path saveDir;

    @AfterEach
    void clearSaveDirOverride() {
        System.clearProperty("ecosphere.save.dir");
    }

    @Test
    void gameSavesAreScopedByUserId() {
        System.setProperty("ecosphere.save.dir", saveDir.toString());
        String userOneId = "test-save-user-one-" + System.nanoTime();
        String userTwoId = "test-save-user-two-" + System.nanoTime();

        GameState userOneState = new GameState();
        userOneState.addResource(Resource.MONEY, 111);

        GameState userTwoState = new GameState();
        userTwoState.addResource(Resource.MONEY, 222);

        new GameSaveManager(userOneId).saveGame(userOneState);
        new GameSaveManager(userTwoId).saveGame(userTwoState);

        GameState loadedOne = new GameLoadManager(userOneId).loadGame();
        GameState loadedTwo = new GameLoadManager(userTwoId).loadGame();

        assertEquals(111, loadedOne.getResourceAmount(Resource.MONEY));
        assertEquals(222, loadedTwo.getResourceAmount(Resource.MONEY));

        GameSaveManager.deleteSaveForUser(userOneId);
        GameSaveManager.deleteSaveForUser(userTwoId);
    }

    @Test
    void newAccountStartsWithFreshResourcesAndPlanetsWhenNoSaveExists() {
        System.setProperty("ecosphere.save.dir", saveDir.toString());
        UserAccountService accountService = new UserAccountService(new UserDatabase());
        UserAccountService.RegistrationResult account =
                accountService.register("fresh_game_user_" + System.nanoTime(), "Pass1234", "Pass1234");
        assertTrue(account.ok());

        GameSaveManager.deleteSaveForUser(account.userId());
        GameState loadedState = new GameLoadManager(account.userId()).loadGame();
        GameState state = loadedState == null ? new GameState() : loadedState;

        GameController controller = new GameController(state);
        if (loadedState == null) {
            controller.initializeNewGame();
        }

        assertEquals(1000, state.getResourceAmount(Resource.MONEY));
        assertEquals(200, state.getResourceAmount(Resource.METAL));
        assertEquals(800, state.getResourceAmount(Resource.WOOD));
        assertEquals(300, state.getResourceAmount(Resource.STONE));
        assertEquals(1, state.getPlanets().size());
        assertNotNull(state.getSelectedPlanet());
        assertNotNull(state.getSelectedPlanet().getTextureID());

        GameSaveManager.deleteSaveForUser(account.userId());
    }
}
