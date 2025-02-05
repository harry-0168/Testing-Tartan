package tartan.smarthome.resources;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TartanSmartDoorLockTest {

    @Test
    void test_lock_fails_with_incorrect_passcode() {
        TartanSmartDoorLock lock = new TartanSmartDoorLock("1234");
        StringBuffer log = new StringBuffer();
        lock.lock("9999", log);

        assertFalse(lock.isLocked(), "Door should remain unlocked with an invalid passcode");
        assertTrue(log.toString().contains("Invalid passcode. Lock request denied."),
                   "Log should contain a message for invalid passcode attempt");
    }

    @Test
    void test_lock_succeeds_with_correct_passcode() {
        TartanSmartDoorLock lock = new TartanSmartDoorLock("1234");
        StringBuffer log = new StringBuffer();

        lock.lock("1234", log);

        assertTrue(lock.isLocked(), "Door should be locked with the correct passcode");
        assertTrue(log.toString().contains("Door LOCKED successfully."),
                   "Log should contain a message for successful locking");
    }

    @Test
    void test_unlock_fails_with_incorrect_passcode() {
        TartanSmartDoorLock lock = new TartanSmartDoorLock("1234");
        StringBuffer log = new StringBuffer();
        lock.lock("1234", log);

        lock.unlock("9999", log);

        assertTrue(lock.isLocked(), "Door should remain locked with an invalid passcode");
        assertTrue(log.toString().contains("Invalid passcode. Unlock request denied."),
                   "Log should contain a message for invalid unlock attempt");
    }

    @Test
    void test_unlock_succeeds_with_correct_passcode() {
        TartanSmartDoorLock lock = new TartanSmartDoorLock("1234");
        StringBuffer log = new StringBuffer();
        lock.lock("1234", log);

        lock.unlock("1234", log);

        assertFalse(lock.isLocked(), "Door should unlock with the correct passcode");
        assertTrue(log.toString().contains("Door UNLOCKED successfully."),
                   "Log should contain a message for successful unlocking");
    }
}
