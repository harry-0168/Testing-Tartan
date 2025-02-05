package tartan.smarthome.resources;

public class TartanSmartDoorLock {

    private final String correctPasscode;
    private boolean locked;

    public TartanSmartDoorLock(String correctPasscode) {
        this.correctPasscode = correctPasscode;
        this.locked = false;
    }

    private String formatLogEntry(String entry) {
        long timeStamp = System.currentTimeMillis();
        return "[" + timeStamp + "]: " + entry;
    }

    public boolean isLocked() {
        return locked;
    }

    private boolean isPasscodeValid(String entered) {
        return entered != null && entered.equals(correctPasscode);
    }

    public void lock(String enteredPasscode, StringBuffer log) {
        if (isPasscodeValid(enteredPasscode)) {
            locked = true;
            log.append(formatLogEntry("Door LOCKED successfully.\n"));
        } else {
            log.append(formatLogEntry("Invalid passcode. Lock request denied.\n"));
        }
    }

    public void unlock(String enteredPasscode, StringBuffer log) {
        if (isPasscodeValid(enteredPasscode)) {
            locked = false;
            log.append(formatLogEntry("Door UNLOCKED successfully.\n"));
        } else {
            log.append(formatLogEntry("Invalid passcode. Unlock request denied.\n"));
        }
    }
}
