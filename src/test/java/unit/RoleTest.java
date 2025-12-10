package unit;

import com.patrick.domain.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoleTest {
    @Test
    public void shouldHaveCorrectRoleValues() {
     Role[] roles = Role.values();
     assertEquals(3, roles.length);

     assertEquals(Role.USER, Role.valueOf("USER"));
     assertEquals(Role.MODEL, Role.valueOf("MODEL"));
     assertEquals(Role.SYSTEM, Role.valueOf("SYSTEM"));
    }

    @Test
    public void shouldReturnCorrectWireValues() {
        assertEquals("user", Role.USER.Wire());
        assertEquals("model", Role.MODEL.Wire());
        assertEquals("system", Role.SYSTEM.Wire());
    }
}
