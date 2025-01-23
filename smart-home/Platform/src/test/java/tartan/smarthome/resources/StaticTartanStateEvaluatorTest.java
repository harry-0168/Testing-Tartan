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

    @Test
    public void test_rule3() {
        // Initial state: House is vacant, door is open
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, true);
        initialState.put(IoTValues.LIGHT_STATE, false);
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

        assertEquals(false, evaluatedState.get(IoTValues.DOOR_STATE), "Door should be CLOSED when the house is vacant.");
    }
      
    public void test_rule9() {
        //Same passcode, should disable alarm
        Map<String, Object> inState1 = new HashMap<>();
        //Placeholder states
        inState1.put(IoTValues.TEMP_READING, 30);
        inState1.put(IoTValues.HUMIDITY_READING, 30);
        inState1.put(IoTValues.TARGET_TEMP, 30);
        inState1.put(IoTValues.HUMIDIFIER_STATE, false);
        inState1.put(IoTValues.DOOR_STATE, false);
        inState1.put(IoTValues.LIGHT_STATE, false);
        inState1.put(IoTValues.HEATER_STATE, false);
        inState1.put(IoTValues.CHILLER_STATE, false);
        inState1.put(IoTValues.HVAC_MODE, "Chiller");
        inState1.put(IoTValues.AWAY_TIMER, false);

        //Meaningful States
        inState1.put(IoTValues.PROXIMITY_STATE, true);
        inState1.put(IoTValues.ALARM_PASSCODE, "quit");
        inState1.put(IoTValues.GIVEN_PASSCODE, "quit");
        inState1.put(IoTValues.ALARM_STATE, false);
        inState1.put(IoTValues.ALARM_ACTIVE, true);

        StringBuffer sb = new StringBuffer();
        Map<String, Object> outState1 = new StaticTartanStateEvaluator().evaluateState(inState1, sb);
        assertEquals(false, outState1.get(IoTValues.ALARM_ACTIVE));

        //Different Passcode
        Map<String, Object> inState2 = new HashMap<>();
        //Placeholder states
        inState2.put(IoTValues.TEMP_READING, 30);
        inState2.put(IoTValues.HUMIDITY_READING, 30);
        inState2.put(IoTValues.TARGET_TEMP, 30);
        inState2.put(IoTValues.HUMIDIFIER_STATE, false);
        inState2.put(IoTValues.DOOR_STATE, false);
        inState2.put(IoTValues.LIGHT_STATE, false);
        inState2.put(IoTValues.HEATER_STATE, false);
        inState2.put(IoTValues.CHILLER_STATE, false);
        inState2.put(IoTValues.HVAC_MODE, "Chiller");
        inState2.put(IoTValues.AWAY_TIMER, false);

        //Meaningful States
        inState2.put(IoTValues.PROXIMITY_STATE, true);
        inState2.put(IoTValues.ALARM_PASSCODE, "quit");
        inState2.put(IoTValues.GIVEN_PASSCODE, "exit");
        inState2.put(IoTValues.ALARM_STATE, false);
        inState2.put(IoTValues.ALARM_ACTIVE, true);

        StringBuffer sb2 = new StringBuffer();
        Map<String, Object> outState2 = new StaticTartanStateEvaluator().evaluateState(inState2, sb2);
        assertEquals(true, outState2.get(IoTValues.ALARM_ACTIVE));

        //Testing the slightly different passcode
        Map<String, Object> inState3 = new HashMap<>();
        //Placeholder states
        inState3.put(IoTValues.TEMP_READING, 30);
        inState3.put(IoTValues.HUMIDITY_READING, 30);
        inState3.put(IoTValues.TARGET_TEMP, 30);
        inState3.put(IoTValues.HUMIDIFIER_STATE, false);
        inState3.put(IoTValues.DOOR_STATE, false);
        inState3.put(IoTValues.LIGHT_STATE, false);
        inState3.put(IoTValues.HEATER_STATE, false);
        inState3.put(IoTValues.CHILLER_STATE, false);
        inState3.put(IoTValues.HVAC_MODE, "Chiller");
        inState3.put(IoTValues.AWAY_TIMER, false);

        //Meaningful States
        inState3.put(IoTValues.PROXIMITY_STATE, true);
        inState3.put(IoTValues.ALARM_PASSCODE, "quit");
        inState3.put(IoTValues.GIVEN_PASSCODE, "1exit");
        inState3.put(IoTValues.ALARM_STATE, false);
        inState3.put(IoTValues.ALARM_ACTIVE, true);

        StringBuffer sb3 = new StringBuffer();
        Map<String, Object> outState3 = new StaticTartanStateEvaluator().evaluateState(inState3, sb3);
        assertEquals(true, outState3.get(IoTValues.ALARM_ACTIVE));
    }
}
