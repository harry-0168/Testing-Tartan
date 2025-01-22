package tartan.smarthome.resources;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import tartan.smarthome.resources.iotcontroller.IoTValues;

public class StaticTartanStateEvaluatorTest {

    @Test
    public void test_rule1() {
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.LIGHT_STATE, true);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.ALARM_ACTIVE, false);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HUMIDITY_READING, 50);
        initialState.put(IoTValues.HVAC_MODE, "Heater");
        initialState.put(IoTValues.AWAY_TIMER, false);
        initialState.put(IoTValues.HEATER_STATE, false);
        initialState.put(IoTValues.CHILLER_STATE, false);
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);
        initialState.put(IoTValues.ALARM_PASSCODE, "1234");
        initialState.put(IoTValues.GIVEN_PASSCODE, "");
        initialState.put(IoTValues.ALARM_STATE, false);

        StringBuffer logBuffer = new StringBuffer();
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);

        assertEquals(false, evaluatedState.get(IoTValues.LIGHT_STATE), "Light should be OFF when the house is vacant.");
    }

    @Test
    public void test_rule10() {
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(IoTValues.PROXIMITY_STATE, true);
        initialState.put(IoTValues.LIGHT_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, true);
        initialState.put(IoTValues.ALARM_ACTIVE, false);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HUMIDITY_READING, 50);
        initialState.put(IoTValues.HVAC_MODE, "Heater");
        initialState.put(IoTValues.AWAY_TIMER, false);
        initialState.put(IoTValues.HEATER_STATE, false);
        initialState.put(IoTValues.CHILLER_STATE, false);
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);
        initialState.put(IoTValues.ALARM_PASSCODE, "1234");
        initialState.put(IoTValues.GIVEN_PASSCODE, "");
        initialState.put(IoTValues.ALARM_STATE, false);

        StringBuffer logBuffer = new StringBuffer();

        // Scenario 1: Target temperature is greater than current temperature
        initialState.put(IoTValues.TEMP_READING, 70);
        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState1.get(IoTValues.HEATER_STATE), "Heater should be ON when the target temperature is greater than the current temperature.");

        logBuffer.setLength(0);
        // Scenario 2: Target temperature is less than current temperature
        initialState.put(IoTValues.TEMP_READING, 74);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState2.get(IoTValues.HEATER_STATE), "Heater should be OFF when the target temperature is less than the current temperature.");
    }
}
