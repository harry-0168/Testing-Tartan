package tartan.smarthome.resources;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import tartan.smarthome.resources.iotcontroller.IoTValues;

public class StaticTartanStateEvaluatorTest {

    @Test
    public void testRule1_LightCannotTurnOnWhenHouseIsVacant() {
        // Create the initial state map with all required keys
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(IoTValues.PROXIMITY_STATE, false); // House is vacant
        initialState.put(IoTValues.LIGHT_STATE, true);     // Light is on
        initialState.put(IoTValues.DOOR_STATE, false);     // Door is closed
        initialState.put(IoTValues.ALARM_ACTIVE, false);    // Alarm is off
        initialState.put(IoTValues.TEMP_READING, 70);      // Current temperature
        initialState.put(IoTValues.TARGET_TEMP, 72);       // Desired temperature
        initialState.put(IoTValues.HUMIDITY_READING, 50);  // Current humidity
        initialState.put(IoTValues.HVAC_MODE, "Heater");   // HVAC mode
        initialState.put(IoTValues.AWAY_TIMER, false);     // Away timer off
        initialState.put(IoTValues.HEATER_STATE, false);   // Heater off
        initialState.put(IoTValues.CHILLER_STATE, false);  // Chiller off
        initialState.put(IoTValues.HUMIDIFIER_STATE, false); // Humidifier off
        initialState.put(IoTValues.ALARM_PASSCODE, "1234"); // Alarm passcode
        initialState.put(IoTValues.GIVEN_PASSCODE, "");     // Empty passcode
        initialState.put(IoTValues.ALARM_STATE, false);

        // Create a StringBuffer for logs
        StringBuffer logBuffer = new StringBuffer();

        // Evaluate the state using StaticTartanStateEvaluator
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);

        // Verify that the light is turned off
        assertEquals(false, evaluatedState.get(IoTValues.LIGHT_STATE), "Light should be OFF when the house is vacant.");

        // Optionally verify logs
        assertTrue(logBuffer.length() > 0, "Log buffer should not be empty.");
    }

    @Test
    public void testRule10_HeaterBehaviorBasedOnTargetTemperature() {
        // Create the initial state map with all required keys
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House is occupied
        initialState.put(IoTValues.LIGHT_STATE, false);     // Light is off
        initialState.put(IoTValues.DOOR_STATE, true);       // Door is open
        initialState.put(IoTValues.ALARM_ACTIVE, false);    // Alarm is off
        initialState.put(IoTValues.TEMP_READING, 70);       // Current temperature
        initialState.put(IoTValues.TARGET_TEMP, 72);        // Desired temperature
        initialState.put(IoTValues.HUMIDITY_READING, 50);   // Current humidity
        initialState.put(IoTValues.HVAC_MODE, "Heater");    // HVAC mode
        initialState.put(IoTValues.AWAY_TIMER, false);      // Away timer off
        initialState.put(IoTValues.HEATER_STATE, false);    // Heater off
        initialState.put(IoTValues.CHILLER_STATE, false);   // Chiller off
        initialState.put(IoTValues.HUMIDIFIER_STATE, false); // Humidifier off
        initialState.put(IoTValues.ALARM_PASSCODE, "1234"); // Alarm passcode
        initialState.put(IoTValues.GIVEN_PASSCODE, "");     // Empty passcode
        initialState.put(IoTValues.ALARM_STATE, false);     // Alarm off

        // Create a StringBuffer for logs
        StringBuffer logBuffer = new StringBuffer();

        // Scenario 1: Target temperature is greater than current temperature
        initialState.put(IoTValues.TEMP_READING, 70);  // Current temperature
        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);

        // Verify that the heater is turned on
        assertEquals(true, evaluatedState1.get(IoTValues.HEATER_STATE), "Heater should be ON when the target temperature is greater than the current temperature.");

        // Clear log for the next scenario
        logBuffer.setLength(0);

        // Scenario 2: Target temperature is less than current temperature
        initialState.put(IoTValues.TEMP_READING, 74);  // Current temperature
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);

        // Verify that the heater is turned off
        assertEquals(false, evaluatedState2.get(IoTValues.HEATER_STATE), "Heater should be OFF when the target temperature is less than the current temperature.");
    }
}
