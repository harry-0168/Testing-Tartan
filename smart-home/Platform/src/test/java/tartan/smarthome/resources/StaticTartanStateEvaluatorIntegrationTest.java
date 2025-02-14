package tartan.smarthome.resources;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.iotcontroller.IoTValues;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticTartanStateEvaluatorIntegrationTest {
    private Map<String, Object> initializeState() {
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HUMIDITY_READING, 50);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.LIGHT_STATE, false);
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.ALARM_STATE, false);
        initialState.put(IoTValues.ALARM_ACTIVE, false);
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);
        initialState.put(IoTValues.HEATER_STATE, false);
        initialState.put(IoTValues.CHILLER_STATE, false);
        initialState.put(IoTValues.HVAC_MODE, "Heater");
        initialState.put(IoTValues.AWAY_TIMER, false);
        initialState.put(IoTValues.ALARM_PASSCODE, "1234");
        initialState.put(IoTValues.GIVEN_PASSCODE, "");
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "");
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.LOCK_REQUEST, "");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, false);
        initialState.put(IoTValues.LOCK_KEYLESS_ENTRY_ENABLE, false);
        initialState.put(IoTValues.ARRIVING_PROXIMITY_STATE, false);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, false);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 615);
        initialState.put(IoTValues.CURRENT_TIME, 1200);
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, false);
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, false);
        initialState.put(IoTValues.PANEL_MESSAGE, false);
        return initialState;
    }

    // When the night lock mode is on and during night time, the user unlock the lock using a password,
    // then open the door and then close the door. The door should automatically lock itself.
    @Test
    public void integrationTest_case1() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();

        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.CURRENT_TIME, 2330);
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.LOCK_REQUEST, "UNLOCK");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        initialState.put(IoTValues.PROXIMITY_STATE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should be unlocked with the correct passcode");
        assertEquals(true, evaluatedState.get(IoTValues.DOOR_STATE), "Door should be opened with the correct passcode");

        evaluatedState.put(IoTValues.DOOR_STATE, true);
        evaluatedState.put(IoTValues.CURRENT_TIME, 2330);
        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(evaluatedState, logBuffer);
        assertEquals(false, evaluatedState1.get(IoTValues.DOOR_STATE), "Door should closed by night lock mode");

        evaluatedState1.put(IoTValues.DOOR_STATE, false);
        evaluatedState1.put(IoTValues.CURRENT_TIME, 2330);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(evaluatedState1, logBuffer);
        assertEquals(false, evaluatedState2.get(IoTValues.DOOR_STATE), "Door should be closed by the user");
        assertEquals(true, evaluatedState2.get(IoTValues.LOCK_STATE), "Door should be locked automatically");
    }

    // When the night lock mode is on and during night time, the user unlock the lock using a password, then open the door and leave. 
    // The door should be automatically closed and the lock should be automatically locked.
    @Test
    public void integrationTest_case2() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();

        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.CURRENT_TIME, 2330);
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_REQUEST, "UNLOCK");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        initialState.put(IoTValues.PROXIMITY_STATE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should be unlocked with the correct passcode");
        assertEquals(true, evaluatedState.get(IoTValues.DOOR_STATE), "Door should be opened with the correct passcode");

        evaluatedState.put(IoTValues.DOOR_STATE, true);
        evaluatedState.put(IoTValues.CURRENT_TIME, 2331);
        evaluatedState.put(IoTValues.PROXIMITY_STATE, false);
        
        // print evaluatedState
        System.out.println(evaluatedState);

        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(evaluatedState, logBuffer);
        assertEquals(false, evaluatedState1.get(IoTValues.DOOR_STATE), "Door should be closed since house is vacant");
        assertEquals(true, evaluatedState1.get(IoTValues.LOCK_STATE), "Door should be locked automatically");
    }

    // When the night lock mode is on and during night time, the user comes back with his phone that is connected with the sensor, 
    // the door should unlock automatically. After the user enters the building he leaves the door open. The door should be automatically closed and locked.
    @Test
    public void integrationTest_case3() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();

        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.CURRENT_TIME, 2330);
        initialState.put(IoTValues.LOCK_KEYLESS_ENTRY_ENABLE, true);
        initialState.put(IoTValues.ARRIVING_PROXIMITY_STATE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should be unlocked automatically");
        assertEquals(true, evaluatedState.get(IoTValues.DOOR_STATE), "Door should be opened automatically");

        evaluatedState.put(IoTValues.CURRENT_TIME, 2331);
        evaluatedState.put(IoTValues.PROXIMITY_STATE, true);

        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(evaluatedState, logBuffer);
        assertEquals(false, evaluatedState1.get(IoTValues.DOOR_STATE), "Door should be closed with night lock mode enabled");
        assertEquals(true, evaluatedState1.get(IoTValues.LOCK_STATE), "Door should be locked automatically");
    }

    // The user unlocks the door with keyless entry and entered, leaved the door open. Then he uses electronic operation to lock and close the door
    @Test
    public void integrationTest_case4() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();

        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.LOCK_KEYLESS_ENTRY_ENABLE, true);
        initialState.put(IoTValues.ARRIVING_PROXIMITY_STATE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should be unlocked automatically with keyless entry");
        assertEquals(true, evaluatedState.get(IoTValues.DOOR_STATE), "Door should be opened automatically with keyless entry");

        evaluatedState.put(IoTValues.LOCK_REQUEST, "LOCK");
        evaluatedState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        evaluatedState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1234");

        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(evaluatedState, logBuffer);
        assertEquals(false, evaluatedState1.get(IoTValues.DOOR_STATE), "Door should be closed by the user");
        assertEquals(true, evaluatedState1.get(IoTValues.LOCK_STATE), "Door should be locked by the user");
    }
}
