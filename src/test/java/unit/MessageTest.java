package unit;

import com.patrick.domain.Message;
import com.patrick.domain.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    @Test
    public void shouldCreateValidMessage() {
        Message message = new Message(Role.USER, "Hello, AI!");
        assertEquals(Role.USER, message.role());
        assertEquals("Hello, AI!", message.text());
    }

    @Test
    public void shouldThrowExceptionWhenRoleIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Message(null, "Hello, AI!")
        );
        assertEquals("Role cannot be null", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenTextIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Message(Role.USER, null)
        );
        assertEquals("Text cannot be null", exception.getMessage());
    }

    @Test
    public void shouldImplementEquals(){
        Message msg1 = new Message(Role.USER, "Hello");
        Message msg2 = new Message(Role.USER, "Hello");
        Message msg3 = new Message(Role.MODEL, "Hello");

        assertEquals(msg1, msg2);
        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "Not a message");
    }

    @Test
    public void shouldImplementHashCode(){
        Message msg1 = new Message(Role.USER, "Hello");
        Message msg2 = new Message(Role.USER, "Hello");

        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    public void shouldImplementToString() {
        Message msg = new Message(Role.USER, "Hello");
        assertEquals("USER: Hello", msg.toString());
    }
}
