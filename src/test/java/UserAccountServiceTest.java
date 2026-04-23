package uk.ac.soton.comp2300;

import org.junit.jupiter.api.Test;
import uk.ac.soton.comp2300.model.user.UserAccountService;
import uk.ac.soton.comp2300.model.user.UserDatabase;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAccountServiceTest {

    @Test
    void registerRejectsDuplicateUsernameIgnoringCaseAndWhitespace() {
        UserDatabase database = new UserDatabase();
        UserAccountService service = new UserAccountService(database);
        String username = "UniqueUser" + System.nanoTime();

        assertTrue(service.register(username, "Pass1234", "Pass1234").ok());
        assertFalse(service.register("  " + username.toUpperCase() + "  ", "Pass1234", "Pass1234").ok());
    }

    @Test
    void registeredUsersReceiveStableUniqueIds() {
        UserDatabase database = new UserDatabase();
        UserAccountService service = new UserAccountService(database);

        UserAccountService.RegistrationResult first =
                service.register("save_user_one_" + System.nanoTime(), "Pass1234", "Pass1234");
        UserAccountService.RegistrationResult second =
                service.register("save_user_two_" + System.nanoTime(), "Pass1234", "Pass1234");

        assertTrue(first.ok());
        assertTrue(second.ok());
        assertNotNull(first.userId());
        assertNotNull(second.userId());
        assertNotEquals(first.userId(), second.userId());
    }
}
