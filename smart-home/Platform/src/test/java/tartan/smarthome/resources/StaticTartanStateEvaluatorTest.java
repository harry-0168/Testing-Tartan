package tartan.smarthome.resources;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import tartan.smarthome.resources.iotcontroller.IoTValues;

public class StaticTartanStateEvaluatorTest {

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
        return initialState;
    }

    // R1: If the house is vacant, then the light cannot be turned on
    @Test
    public void test_rule1() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.LIGHT_STATE, true);

        // case 1: house is vacant, light is on, light should be turned off
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LIGHT_STATE), "Light should be OFF when the house is vacant.");

        // case 2: house is vacant, light is off, light should remain off
        initialState.put(IoTValues.LIGHT_STATE, false);
        logBuffer.setLength(0);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState2.get(IoTValues.LIGHT_STATE), "Light should remain OFF when the house is vacant.");
    }

    // R3: If the house is vacant, then close the door.
    @Test
    public void test_rule3() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();

        // case 1: house is vacant, door is open, door should be closed
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, true);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.DOOR_STATE), "Door should be CLOSED when the house is vacant.");
    
        // case 2: house is vacant, door is closed, door should remain closed
        initialState.put(IoTValues.DOOR_STATE, false);
        logBuffer.setLength(0);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState2.get(IoTValues.DOOR_STATE), "Door should remain CLOSED when the house is vacant.");
    }

    // R9: The correct passcode is required to disable the alarm.
    // @Test
    // public void test_rule9() {
    //     // case 1: correct passcode is entered, alarm should be turned off
    //     Map<String, Object> inState1 = initializeState();
    //     StringBuffer sb = new StringBuffer();
    //     inState1.put(IoTValues.PROXIMITY_STATE, true);
    //     inState1.put(IoTValues.ALARM_PASSCODE, "1234");
    //     inState1.put(IoTValues.GIVEN_PASSCODE, "1234");
    //     inState1.put(IoTValues.ALARM_STATE, false);
    //     inState1.put(IoTValues.ALARM_ACTIVE, true);
    //     Map<String, Object> outState1 = new StaticTartanStateEvaluator().evaluateState(inState1, sb);
    //     assertEquals(false, outState1.get(IoTValues.ALARM_ACTIVE));

    //     // case 2: incorrect passcode is entered, alarm should remain active
    //     Map<String, Object> inState2 = initializeState();
    //     StringBuffer sb2 = new StringBuffer();
    //     inState2.put(IoTValues.PROXIMITY_STATE, true);
    //     inState2.put(IoTValues.ALARM_PASSCODE, "1234");
    //     inState2.put(IoTValues.GIVEN_PASSCODE, "123");
    //     inState2.put(IoTValues.ALARM_STATE, false);
    //     inState2.put(IoTValues.ALARM_ACTIVE, true);
    //     Map<String, Object> outState2 = new StaticTartanStateEvaluator().evaluateState(inState2, sb2);
    //     assertEquals(true, outState2.get(IoTValues.ALARM_ACTIVE));

    //     // case 3: no passcode is entered, alarm should remain active
    //     Map<String, Object> inState3 = initializeState();
    //     StringBuffer sb3 = new StringBuffer();
    //     inState3.put(IoTValues.PROXIMITY_STATE, true);
    //     inState3.put(IoTValues.ALARM_PASSCODE, "1234");
    //     inState3.put(IoTValues.GIVEN_PASSCODE, "");
    //     inState3.put(IoTValues.ALARM_STATE, false);
    //     inState3.put(IoTValues.ALARM_ACTIVE, true);
    //     Map<String, Object> outState3 = new StaticTartanStateEvaluator().evaluateState(inState3, sb3);
    //     assertEquals(true, outState3.get(IoTValues.ALARM_ACTIVE));
    // }

    // R10: If the target temperature is greater than the current temperature, then turn on the heater.
    // Otherwise, turn off the heater
    @Test
    public void test_rule10() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        // case 1: target temperature is greater than current temperature, heater should be turned on
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HEATER_STATE, false);
        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState1.get(IoTValues.HEATER_STATE), "Heater should be ON when the target temperature is greater than the current temperature.");

        // case 2: target temperature is less than current temperature, heater should be turned off
        logBuffer.setLength(0);
        initialState.put(IoTValues.TEMP_READING, 74);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HEATER_STATE, true);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState2.get(IoTValues.HEATER_STATE), "Heater should be OFF when the target temperature is less than the current temperature.");
    }

    // R12: The heater and the dehumidifier cannot be run simultaneously.
    @Test
    public void test_rule12() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        // case 1: heater is on and temperature is less than target temperature, dehumidifier should be turned off
        initialState.put(IoTValues.HEATER_STATE, true);
        initialState.put(IoTValues.HUMIDIFIER_STATE, true);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(true, evaluatedState.get(IoTValues.HEATER_STATE)); // heater must stay On
        assertEquals(false, evaluatedState.get(IoTValues.HUMIDIFIER_STATE)); // dehumidifier should turn off

        // case 2: heater is on and temperature is greater than target temperature, dehumidifier should be on while heater is off
        logs.setLength(0);
        initialState.put(IoTValues.HEATER_STATE, true);
        initialState.put(IoTValues.HUMIDIFIER_STATE, true);
        initialState.put(IoTValues.TEMP_READING, 74);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(false, evaluatedState2.get(IoTValues.HEATER_STATE)); // heater must turn off
        assertEquals(true, evaluatedState2.get(IoTValues.HUMIDIFIER_STATE)); // dehumidifier should turn on
    }
}
