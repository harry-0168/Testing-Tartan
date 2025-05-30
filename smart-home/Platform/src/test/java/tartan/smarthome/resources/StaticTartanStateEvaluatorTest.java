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
    // Case 1: If house is vacant, and the door is open. Then the door is closed.
    @Test
    public void test_rule3_case1() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, true);
        Map<String, Object> evaluatedState1 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState1.get(IoTValues.DOOR_STATE), "Door should be CLOSED when the house is vacant.");
    }

    // Case 2: If house is vacant, and the door is already closed. Then door should remain closed.
    @Test
    public void test_rule3_case2() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState2.get(IoTValues.DOOR_STATE), "Door should remain CLOSED when the house is vacant.");
    }

    // Case 3: If house is vacant, door is opened and the alarm is enabled. Then door should remain open.
    @Test
    public void test_rule3_case3() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, true);
        initialState.put(IoTValues.ALARM_STATE, true);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.DOOR_STATE), "Door should remain OPEN when the house is vacant, alarm is enabled and door is opened.");
    }

    // Case 4: If house is vacant, alarm is enabled and door is closed. Then door should remain closed.
    @Test
    public void test_rule3_case4() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.ALARM_STATE, true);
        Map<String, Object> evaluatedState4 = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState4.get(IoTValues.DOOR_STATE), "Door should remain CLOSED when the house is vacant and alarm is enabled.");
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

    // case 7: user shouldn't be able to disable alarm when the proximity state is false even with correct passcode
    @Test
    public void test_rule9_case7() {
        Map<String, Object> inState7 = initializeState();
        StringBuffer sb7 = new StringBuffer();
        inState7.put(IoTValues.PROXIMITY_STATE, false);
        inState7.put(IoTValues.ALARM_PASSCODE, "1234");
        inState7.put(IoTValues.GIVEN_PASSCODE, "1234");
        inState7.put(IoTValues.ALARM_STATE, false);
        inState7.put(IoTValues.ALARM_ACTIVE, true);
        Map<String, Object> outState7 = new StaticTartanStateEvaluator().evaluateState(inState7, sb7);
        assertEquals(true, outState7.get(IoTValues.ALARM_ACTIVE));
        assertEquals(true, outState7.get(IoTValues.ALARM_STATE));
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
    public void test_rule12_case1() {
        /* case 1: Input: current temperature is less than target temperature, heater is ON and dehumidifier is ON
         *         Output: heater stays ON and dehumidifier is turned OFF
         */
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.HEATER_STATE, true);
        initialState.put(IoTValues.HUMIDIFIER_STATE, true);
        initialState.put(IoTValues.TEMP_READING, 60);
        initialState.put(IoTValues.TARGET_TEMP, 70);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(true, evaluatedState.get(IoTValues.HEATER_STATE)); // heater must stay On
        assertEquals(false, evaluatedState.get(IoTValues.HUMIDIFIER_STATE)); // dehumidifier should turn off

    }
    @Test
    public void test_rule12_case2(){
        /* case 2: Input: current temperature is greater than target temperature, heater is ON and dehumidifier is ON
         *         Output: heater turns OFF and dehumidifier stays ON
         */
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.HEATER_STATE, true);
        initialState.put(IoTValues.HUMIDIFIER_STATE, true);
        initialState.put(IoTValues.TEMP_READING, 72);
        initialState.put(IoTValues.TARGET_TEMP, 70);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.HEATER_STATE));
        assertEquals(true, evaluatedState.get(IoTValues.HUMIDIFIER_STATE));

    }
    @Test
    public void test_rule12_case3(){
        /* case 3: Input: current temperature is equal to target temperature, heater is ON and dehumidifier is ON
         *         Output: heater turns OFF and dehumidifier turns OFF (since chiller is OFF)
         */
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.HEATER_STATE, true);
        initialState.put(IoTValues.HUMIDIFIER_STATE, true);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 70);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.HEATER_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.HUMIDIFIER_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.CHILLER_STATE));

    }
    @Test
    public void test_rule12_case4(){
        /* case 4: Input: current temperature is less than target temperature, heater is OFF and dehumidifier is ON
         *         Output: heater turns ON and dehumidifier turns OFF
         */
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.HEATER_STATE, false);
        initialState.put(IoTValues.HUMIDIFIER_STATE, true);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.HEATER_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.HUMIDIFIER_STATE));

    }
    @Test
    public void test_rule12_case5(){
        /* case 5: Input: current temperature greater than target temperature, heater is ON and dehumidifier is OFF, chiller is OFF
         *         Output: heater turns OFF, dehumidifier turns ON and chiller turns ON
         */
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.HEATER_STATE, true);
        initialState.put(IoTValues.HUMIDIFIER_STATE, true);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 60);
        initialState.put(IoTValues.CHILLER_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.HEATER_STATE));
        assertEquals(true, evaluatedState.get(IoTValues.CHILLER_STATE));
        assertEquals(true, evaluatedState.get(IoTValues.HUMIDIFIER_STATE));
    }
  
    @Test
    public void test_rule12_case6(){
        /* case 6: Input: current temperature is less target temperature, heater is OFF and dehumidifier is OFF
         *         Output: heater turns ON and dehumidifier stays OFF
         */
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.HEATER_STATE, false);
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 72);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.HEATER_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.HUMIDIFIER_STATE));
    }
  
    @Test
    public void test_rule12_case7(){
        /* case 7: Input: current temperature greater than target temperature, heater is OFF and dehumidifier is OFF
         *         Output: heater turns OFF, dehumidifier stays OFF
         */
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.HEATER_STATE, false);
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);
        initialState.put(IoTValues.TEMP_READING, 70);
        initialState.put(IoTValues.TARGET_TEMP, 60);
        initialState.put(IoTValues.CHILLER_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.HEATER_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.HUMIDIFIER_STATE));
    }

    // Test for smart door lock and unlock with correct passcode
    @Test
    public void test_lock_unlock_succeeds_with_correct_passcode() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.LOCK_REQUEST, "LOCK");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should be locked with the correct passcode");
        assertTrue(logs.toString().contains("Door locked"),
                   "Log should contain a message for successful locking");

        logs.setLength(0);
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.LOCK_REQUEST, "UNLOCK");
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(false, evaluatedState2.get(IoTValues.LOCK_STATE), "Door should be unlocked with the correct passcode");
    }

    // Test for smart door lock with incorrect passcode
    @Test
    public void test_lock_fails_with_incorrect_passcode() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "4321");
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.LOCK_REQUEST, "LOCK");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should not be locked with the incorrect passcode");
        assertTrue(logs.toString().contains("Invalid passcode given to lock door"),
                   "Log should contain a message for unsuccessful locking");
    }
  
    // Test for smart door unlock with incorrect passcode
    @Test
    public void test_unlock_fails_with_incorrect_passcode() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "4321");
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.LOCK_REQUEST, "UNLOCK");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should remain locked with the incorrect passcode");
        assertTrue(logs.toString().contains("Invalid passcode given to unlock door"),
                   "Log should contain a message for unsuccessful unlocking");
    }

    // Test for smart door lock with disabled electronic operation
    @Test
    public void test_lock_fails_with_disabled_electronic_operation() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.LOCK_REQUEST, "LOCK");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, false);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should not be locked with disabled electronic operation");
        assertTrue(logs.toString().contains("Electronic operation of lock is disabled"),
                   "Log should contain a message for unsuccessful locking");
    }

    // Test for smart door keyless entry with enabled keyless entry
    @Test
    public void test_keyless_entry_succeeds_with_enabled_keyless_entry() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.LOCK_KEYLESS_ENTRY_ENABLE, true);
        initialState.put(IoTValues.ARRIVING_PROXIMITY_STATE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should be unlocked with keyless entry when arriving home");
        assertTrue(logs.toString().contains("Arriving home, automatically unlocking door"),
                   "Log should contain a message for successful locking");
    }

    // Test for smart door keyless entry with disabled keyless entry
    @Test
    public void test_keyless_entry_fails_with_disabled_keyless_entry() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.LOCK_KEYLESS_ENTRY_ENABLE, false);
        initialState.put(IoTValues.ARRIVING_PROXIMITY_STATE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should remain locked with disabled keyless entry");
        assertTrue(logs.toString().contains("Arriving home, keyless entry disabled"),
                   "Log should contain a message for unsuccessful locking");
    }
    
    // Test for smart door lock night lock configuration
    @Test
    public void test_nightLockConfiguration() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230); // 10:30 PM
        initialState.put(IoTValues.NIGHT_END_TIME, 615); // 6:15 AM
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_NIGHT_LOCK_ENABLED), "Night lock enabled");
        assertEquals(2230, evaluatedState.get(IoTValues.NIGHT_START_TIME), "Night start time should be 2230");
        assertEquals(615, evaluatedState.get(IoTValues.NIGHT_END_TIME), "Night end time should be 615");
    }

    // Test automatic door locking during nighttime
    @Test
    public void test_nighttime_auto_lock() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 615);
        initialState.put(IoTValues.CURRENT_TIME, 2330);
        initialState.put(IoTValues.LOCK_PASSCODE, "1111");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1111");
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should automatically be locked during nighttime");
    }

    // Test when night lock mode is disabled, ensuring no automatic locking occurs
    @Test
    public void test_nighttime_auto_lock_disabled() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, false);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 615);
        initialState.put(IoTValues.CURRENT_TIME, 2300);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should not automatically lock when night lock mode is disabled");
    }


    // Test automatic door locking during nighttime when the end time is before midnight
    @Test
    public void test_nighttime_auto_lock_end_before_midnight() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 2359);
        initialState.put(IoTValues.CURRENT_TIME, 2330);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should automatically be locked during nighttime");
    }

    // Test nighttime detection the end time end before midnight and it is currently not in nighttime
    // Expect no change in the door lock state and no automatic lock
    @Test
    public void test_nighttime_auto_lock_end_before_midnight_not_in_nighttime() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 2320);
        initialState.put(IoTValues.CURRENT_TIME, 2330);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should not automatically be locked outside of nighttime");
    }

    // Test nighttime detection the end time end after midnight and it is currently not in nighttime
    // Expect no change in the door lock state and no automatic lock
    @Test
    public void test_nighttime_auto_lock_end_after_midnight_not_in_nighttime() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 30);
        initialState.put(IoTValues.CURRENT_TIME, 200);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE), "Door should not automatically be locked outside of nighttime");
    }

    // Test automatic door locking during nighttime when current time right on edge of end time
    @Test
    public void test_nighttime_auto_lock_right_on_nighttime() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 615);
        initialState.put(IoTValues.CURRENT_TIME, 2230);
        initialState.put(IoTValues.LOCK_PASSCODE, "1111");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1111");
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should automatically be locked during nighttime");
    }
    // Test automatic door locking during nighttime when current time right on edge of start time
    @Test
    public void test_nighttime_auto_lock_right_on_nighttime2() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 615);
        initialState.put(IoTValues.CURRENT_TIME, 615);
        initialState.put(IoTValues.LOCK_PASSCODE, "1111");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1111");
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should automatically be locked during nighttime");
    }
    // Test automatic door locking during nighttime when the end time is before midnight
    // when current time right on edge of end time
    @Test
    public void test_nighttime_auto_lock_end_before_midnight_right_on_nighttime() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 2359);
        initialState.put(IoTValues.CURRENT_TIME, 2359);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should automatically be locked during nighttime");
    }
    // Test automatic door locking during nighttime when the end time is before midnight
    // when current time right on edge of start time
    @Test
    public void test_nighttime_auto_lock_end_before_midnight_right_on_nighttime2() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);
        initialState.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, true);
        initialState.put(IoTValues.NIGHT_START_TIME, 2230);
        initialState.put(IoTValues.NIGHT_END_TIME, 2359);
        initialState.put(IoTValues.CURRENT_TIME, 2230);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should automatically be locked during nighttime");
    }

    @Test
    public void intruder_Defense_testCase1(){
        /* case 1: Input: LockIntruderSensorMode is OFF, IntruderDetectionSensor is true, LOCK_STATE is false(unlocked),
         *                  DOOR_STATE is false(closed), panelMessage is "possible intruder detected"
         *         Output: LockIntruderSensorMode is OFF, IntruderDetectionSensor is false, LOCK_STATE is false(unlocked),
         *                  DOOR_STATE is false(closed), panelMessage is "" (empty)
         */

        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, false);
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, true);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.PANEL_MESSAGE, true);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_INTRUDER_SENSOR_MODE));
        assertEquals(true, evaluatedState.get(IoTValues.INTRUDER_DETECTION_SENSOR));
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.DOOR_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.PANEL_MESSAGE));

    }
    @Test
    public void intruder_Defense_testCase2(){
        /* case 2: Input: LockIntruderSensorMode is ON, IntruderDetectionSensor is true, LOCK_STATE is false(unlocked),
         *                  DOOR_STATE is false(closed), panelMessage is "all clear"
         *         Output: LockIntruderSensorMode is ON, IntruderDetectionSensor is true, LOCK_STATE is true(locked),
         *                  DOOR_STATE is false(closed), panelMessage is "possible intruder detected"
         */

        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, true);
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, true);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.PANEL_MESSAGE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_INTRUDER_SENSOR_MODE));
        assertEquals(true, evaluatedState.get(IoTValues.INTRUDER_DETECTION_SENSOR));
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.DOOR_STATE));
        assertEquals(true, evaluatedState.get(IoTValues.PANEL_MESSAGE));

    }
    @Test
    public void intruder_Defense_testCase3(){
        /* case 3: Input: LockIntruderSensorMode is ON, IntruderDetectionSensor is true, LOCK_STATE is true(locked),
         *                  DOOR_STATE is false(closed), panelMessage is "possible intruder detected"
         *         Output: LockIntruderSensorMode is ON, IntruderDetectionSensor is true, LOCK_STATE is true(locked),
         *                  DOOR_STATE is false(closed), panelMessage is "possible intruder detected"
         */

        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, true);
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, true);
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.PANEL_MESSAGE, true);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_INTRUDER_SENSOR_MODE));
        assertEquals(true, evaluatedState.get(IoTValues.INTRUDER_DETECTION_SENSOR));
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.DOOR_STATE));
        assertEquals(true, evaluatedState.get(IoTValues.PANEL_MESSAGE));

    }
    @Test
    public void intruder_Defense_testCase4(){
        /* case 4: Input: PROXIMITY_STATE is true (house occupied, door open),LockIntruderSensorMode is ON, IntruderDetectionSensor is true,
         *                  LOCK_STATE is false(unlocked, DOOR_STATE is true(open), panelMessage is "all clear"
         *         Output: PROXIMITY_STATE is true (house occupied, door open), LockIntruderSensorMode is ON, IntruderDetectionSensor is true,
         *                  LOCK_STATE is false(unlocked), DOOR_STATE is true(open), panelMessage is "possible intruder detected"
         */

        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.PROXIMITY_STATE, true);
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, true);
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, true);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, true);
        initialState.put(IoTValues.PANEL_MESSAGE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_INTRUDER_SENSOR_MODE));
        assertEquals(true, evaluatedState.get(IoTValues.INTRUDER_DETECTION_SENSOR));
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE));
        assertEquals(true, evaluatedState.get(IoTValues.DOOR_STATE));
        assertEquals(true, evaluatedState.get(IoTValues.PANEL_MESSAGE));

    }

    @Test
    public void intruder_Defense_testCase5(){
        /* case 5: Input: LockIntruderSensorMode is ON, IntruderDetectionSensor is false, LOCK_STATE is true(locked),
         *                  DOOR_STATE is false(closed), panelMessage is "possible intruder detected"
         *         Output: LockIntruderSensorMode is ON, IntruderDetectionSensor is false, LOCK_STATE is true(locked),
         *                  DOOR_STATE is false(closed), panelMessage is "all clear"
         */

        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, true);
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, false);
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.PANEL_MESSAGE, true);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_INTRUDER_SENSOR_MODE));
        assertEquals(false, evaluatedState.get(IoTValues.INTRUDER_DETECTION_SENSOR));
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.DOOR_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.PANEL_MESSAGE));

    }
    @Test
    public void intruder_Defense_testCase6(){
        /* case 6: Input: LockIntruderSensorMode is ON, IntruderDetectionSensor is false, LOCK_STATE is false(unlocked),
         *                  DOOR_STATE is false(closed), panelMessage is "all clear"
         *         Output: LockIntruderSensorMode is ON, IntruderDetectionSensor is false, LOCK_STATE is false(unlocked),
         *                  DOOR_STATE is false(closed), panelMessage is "all clear"
         */

        Map<String, Object> initialState = initializeState();
        StringBuffer logBuffer = new StringBuffer();
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, true);
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, false);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, false);
        initialState.put(IoTValues.PANEL_MESSAGE, false);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logBuffer);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_INTRUDER_SENSOR_MODE));
        assertEquals(false, evaluatedState.get(IoTValues.INTRUDER_DETECTION_SENSOR));
        assertEquals(false, evaluatedState.get(IoTValues.LOCK_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.DOOR_STATE));
        assertEquals(false, evaluatedState.get(IoTValues.PANEL_MESSAGE));

    }

    // Test the lock's performance when it's already locked with
    // instruction lock and already unlocked with instruction unlock
    @Test
    public void test_electronic_operation_instructions_same_as_current_state(){
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.LOCK_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_GIVEN_PASSCODE, "1234");
        initialState.put(IoTValues.LOCK_STATE, true);
        initialState.put(IoTValues.LOCK_REQUEST, "LOCK");
        initialState.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, true);

        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(true, evaluatedState.get(IoTValues.LOCK_STATE), "Door should remain locked");
        assertTrue(logs.toString().contains("Door already locked"),
                "Log should contain a message for confirming door already locked");

        logs.setLength(0);
        initialState.put(IoTValues.LOCK_STATE, false);
        initialState.put(IoTValues.LOCK_REQUEST, "UNLOCK");
        Map<String, Object> evaluatedState2 = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertEquals(false, evaluatedState2.get(IoTValues.LOCK_STATE), "Door should remain unlocked");
        assertTrue(logs.toString().contains("Door already unlocked"),
                "Log should contain a message for confirming door already unlocked");
    }

    @Test
    public void test_panel_message() {
        Map<String, Object> initialState = initializeState();
        StringBuffer logs = new StringBuffer();
        initialState.put(IoTValues.INTRUDER_DETECTION_SENSOR, true);
        initialState.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, true);
        Map<String, Object> evaluatedState = new StaticTartanStateEvaluator().evaluateState(initialState, logs);
        assertTrue(logs.toString().contains("Panel Message: Possbiel Intruder detected! Please check the house!"),
                "Log should contain a message for confirming door already unlocked");
    }
}
