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
    // Case 1: house is vacant, light is on, light should be turned off
    @Test
    public void test_rule1_case1() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.LIGHT_STATE, true);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LIGHT_STATE), "Light should be OFF when the house is vacant.");
    }

    // Case 2: house is vacant, light is off, light should remain off
    @Test
    public void test_rule1_case2() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.LIGHT_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LIGHT_STATE), "Light should remain OFF when the house is vacant.");
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
    // case 1: correct passcode is entered, alarm should be turned off
    @Test
    public void test_rule9_case1() {
        Map<String, Object> inState1 = initializeState();
        StringBuffer sb = new StringBuffer();
        inState1.put(IoTValues.PROXIMITY_STATE, true);
        inState1.put(IoTValues.ALARM_PASSCODE, "1234");
        inState1.put(IoTValues.GIVEN_PASSCODE, "1234");
        inState1.put(IoTValues.ALARM_STATE, false);
        inState1.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState1 = new StaticTartanStateEvaluator().evaluateState(inState1, sb);
        assertEquals(false, outState1.get(IoTValues.ALARM_ACTIVE));
    }

    // case 2: incorrect passcode is entered, alarm should remain active. The input passcode is the same
    // length but different.
    @Test
    public void test_rule9_case2() {
        Map<String, Object> inState2 = initializeState();
        StringBuffer sb2 = new StringBuffer();
        inState2.put(IoTValues.PROXIMITY_STATE, true);
        inState2.put(IoTValues.ALARM_PASSCODE, "1234");
        inState2.put(IoTValues.GIVEN_PASSCODE, "2345");
        inState2.put(IoTValues.ALARM_STATE, false);
        inState2.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState2 = new StaticTartanStateEvaluator().evaluateState(inState2, sb2);
        assertEquals(true, outState2.get(IoTValues.ALARM_ACTIVE));
    }

    // case 3: no passcode is entered, alarm should remain active.
    // Input passcode is empty.
    @Test
    public void test_rule9_case3() {
        Map<String, Object> inState3 = initializeState();
        StringBuffer sb3 = new StringBuffer();
        inState3.put(IoTValues.PROXIMITY_STATE, true);
        inState3.put(IoTValues.ALARM_PASSCODE, "1234");
        inState3.put(IoTValues.GIVEN_PASSCODE, "");
        inState3.put(IoTValues.ALARM_STATE, false);
        inState3.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState3 = new StaticTartanStateEvaluator().evaluateState(inState3, sb3);
        assertEquals(true, outState3.get(IoTValues.ALARM_ACTIVE));
    }

    // case 4: both passcode and alarm_passcode are empty, alarm should be disabled
    @Test
    public void test_rule9_case4() {
        Map<String, Object> inState4 = initializeState();
        StringBuffer sb4 = new StringBuffer();
        inState4.put(IoTValues.PROXIMITY_STATE, true);
        inState4.put(IoTValues.ALARM_PASSCODE, "");
        inState4.put(IoTValues.GIVEN_PASSCODE, "");
        inState4.put(IoTValues.ALARM_STATE, false);
        inState4.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState4 = new StaticTartanStateEvaluator().evaluateState(inState4, sb4);
        assertEquals(false, outState4.get(IoTValues.ALARM_ACTIVE));
    }

    // case 5: passcode diff by length, alarm should remain on
    @Test
    public void test_rule9_case5() {
        Map<String, Object> inState5 = initializeState();
        StringBuffer sb5 = new StringBuffer();
        inState5.put(IoTValues.PROXIMITY_STATE, true);
        inState5.put(IoTValues.ALARM_PASSCODE, "1234");
        inState5.put(IoTValues.GIVEN_PASSCODE, "12345");
        inState5.put(IoTValues.ALARM_STATE, false);
        inState5.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState5 = new StaticTartanStateEvaluator().evaluateState(inState5, sb5);
        assertEquals(true, outState5.get(IoTValues.ALARM_ACTIVE));
    }

    // case 6: passcode diff by length, shorter and the first couple index match. Should remain on
    @Test
    public void test_rule9_case6() {
        Map<String, Object> inState6 = initializeState();
        StringBuffer sb6 = new StringBuffer();
        inState6.put(IoTValues.PROXIMITY_STATE, true);
        inState6.put(IoTValues.ALARM_PASSCODE, "1234");
        inState6.put(IoTValues.GIVEN_PASSCODE, "123");
        inState6.put(IoTValues.ALARM_STATE, false);
        inState6.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState6 = new StaticTartanStateEvaluator().evaluateState(inState6, sb6);
        assertEquals(true, outState6.get(IoTValues.ALARM_ACTIVE));
    }

    // Testing when the active state is false for full conditional coverage
    // When active state is false it shouldn't be changed by passcode.
    // Input correct passcode
    @Test
    public void test_rule9_case7() {
        Map<String, Object> inState7 = initializeState();
        StringBuffer sb7 = new StringBuffer();
        inState7.put(IoTValues.PROXIMITY_STATE, true);
        inState7.put(IoTValues.ALARM_PASSCODE, "1234");
        inState7.put(IoTValues.GIVEN_PASSCODE, "1234");
        inState7.put(IoTValues.ALARM_STATE, false);
        inState7.put(IoTValues.ALARM_ACTIVE, false);
        Map<String, Object> outState7 = new StaticTartanStateEvaluator().evaluateState(inState7, sb7);
        assertEquals(false, outState7.get(IoTValues.ALARM_ACTIVE));
    }

    // Testing when the active state is false for full conditional coverage
    // When active state is false it shouldn't be changed by passcode.
    // Input incorrect passcode
    @Test
    public void test_rule9_case8() {
        Map<String, Object> inState8 = initializeState();
        StringBuffer sb8 = new StringBuffer();
        inState8.put(IoTValues.PROXIMITY_STATE, true);
        inState8.put(IoTValues.ALARM_PASSCODE, "1234");
        inState8.put(IoTValues.GIVEN_PASSCODE, "1234");
        inState8.put(IoTValues.ALARM_STATE, false);
        inState8.put(IoTValues.ALARM_ACTIVE, false);
        Map<String, Object> outState8 = new StaticTartanStateEvaluator().evaluateState(inState8, sb8);
        assertEquals(false, outState8.get(IoTValues.ALARM_ACTIVE));
    }
    // Discovered a bug for rule 8.
    // We discovered this while testing for rule 9
    // Was testing if the no user in house the alarm can not be disabled even with the correct passcode
    @Test
    public void test_rule8_case1() {
        Map<String, Object> inState1 = initializeState();
        StringBuffer sb1 = new StringBuffer();
        inState1.put(IoTValues.PROXIMITY_STATE, false);
        inState1.put(IoTValues.ALARM_PASSCODE, "1234");
        inState1.put(IoTValues.GIVEN_PASSCODE, "1234");
        inState1.put(IoTValues.ALARM_STATE, false);
        inState1.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState1 = new StaticTartanStateEvaluator().evaluateState(inState1, sb1);
        assertEquals(true, outState1.get(IoTValues.ALARM_ACTIVE));
        assertEquals(true, outState1.get(IoTValues.ALARM_STATE));
    }

    // R10: If the target temperature is greater than the current temperature, then turn on the heater.
    // Otherwise, turn off the heater
    // the target temperature can be set to 50-80 degrees
    // Case 1: target temperature is greater than current temperature, heater should be turned on
    @Test
    public void test_rule10_case1() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HEATER_STATE, false);
        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState1.get(IoTValues.HEATER_STATE), "Heater should be ON when the target temperature is greater than the current temperature.");
    }

    // Case 2: target temperature is less than current temperature, heater should be turned off
    @Test
    public void test_rule10_case2() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.TEMP_READING, 74);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HEATER_STATE, true);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState2.get(IoTValues.HEATER_STATE), "Heater should be OFF when the target temperature is less than the current temperature.");
    }

    // Case 3: current temperature is less than 50, heater should be turned on
    @Test
    public void test_rule10_case3() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.TEMP_READING, 49);
        initialState.put(IoTValues.TARGET_TEMP, 50);
        initialState.put(IoTValues.HEATER_STATE, false);
        Map<String, Object> evaluatedState3 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState3.get(IoTValues.HEATER_STATE), "Heater should be ON when the target temperature is greater than the current temperature.");
    }

    // Case 4: current temperature is greater than 80, heater should be turned off
    @Test
    public void test_rule10_case4() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.TEMP_READING, 81);
        initialState.put(IoTValues.TARGET_TEMP, 80);
        initialState.put(IoTValues.HEATER_STATE, true);
        Map<String, Object> evaluatedState4 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState4.get(IoTValues.HEATER_STATE), "Heater should be OFF when the target temperature is less than the current temperature.");
    }

    // Case 5: current temperature is equal to target temperature, heater should be be off
    @Test
    public void test_rule10_case5() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.TEMP_READING, 72);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        initialState.put(IoTValues.HEATER_STATE, true);
        Map<String, Object> evaluatedState5 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState5.get(IoTValues.HEATER_STATE), "Heater should be OFF when the target temperature is equal to the current temperature.");
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
