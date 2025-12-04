package unit;

import com.patrick.infra.config.Env;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnvTest {
    @Test
    public void shouldGetExistingEnvVar() {
        String path = Env.get("PATH", false);
        assertNotNull(path);
        assertFalse(path.isBlank());
    }

    @Test
    public void shouldThrowForMissingRequiredVar() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> Env.get("NOT_EXISTENT_VARIABLE", true));
        assertTrue(exception.getMessage().contains("Missing required env variable: "));
    }

    @Test
    public void shouldReturnDefaultWhenVarDoesNotExist() {
        String value = Env.getOrDefault("NOT_EXISTENT_VARIABLE", "default_value");
        assertEquals("default_value", value);
    }

    @Test
    public void shouldReturnEnvVariableWhenExists() {
        String path = Env.getOrDefault("PATH", "default");
        assertNotEquals("default", path);
        assertFalse(path.isBlank());
    }
}