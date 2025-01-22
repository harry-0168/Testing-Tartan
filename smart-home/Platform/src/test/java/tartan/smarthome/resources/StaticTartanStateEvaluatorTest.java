package tartan.smarthome.resources;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import tartan.smarthome.resources.iotcontroller.IoTValues;

public class StaticTartanStateEvaluatorTest {
    @Test
    public void testRule9SamePasscode() {
        Map<String, Object> inState = new HashMap<>();
        //Placeholder states
        inState.put(IoTValues.TEMP_READING, 30);
        inState.put(IoTValues.HUMIDITY_READING, 30);
        inState.put(IoTValues.TARGET_TEMP, 30);
        inState.put(IoTValues.HUMIDIFIER_STATE, false);
        inState.put(IoTValues.DOOR_STATE, false);
        inState.put(IoTValues.LIGHT_STATE, false);
        inState.put(IoTValues.HEATER_STATE, false);
        inState.put(IoTValues.CHILLER_STATE, false);
        inState.put(IoTValues.HVAC_MODE, "Chiller");
        inState.put(IoTValues.AWAY_TIMER, false);

        //Meaningful States
        inState.put(IoTValues.PROXIMITY_STATE, true);
        inState.put(IoTValues.ALARM_PASSCODE, "quit");
        inState.put(IoTValues.GIVEN_PASSCODE, "quit");
        inState.put(IoTValues.ALARM_STATE, false);
        inState.put(IoTValues.ALARM_ACTIVE, true);

        StringBuffer sb = new StringBuffer();

        Map<String, Object> outState = new StaticTartanStateEvaluator().evaluateState(inState, sb);

        assertEquals(false, outState.get(IoTValues.ALARM_ACTIVE));
    }

    @Test
    public void testRule9DifferentPasscode() {
        Map<String, Object> inState = new HashMap<>();
        //Placeholder states
        inState.put(IoTValues.TEMP_READING, 30);
        inState.put(IoTValues.HUMIDITY_READING, 30);
        inState.put(IoTValues.TARGET_TEMP, 30);
        inState.put(IoTValues.HUMIDIFIER_STATE, false);
        inState.put(IoTValues.DOOR_STATE, false);
        inState.put(IoTValues.LIGHT_STATE, false);
        inState.put(IoTValues.HEATER_STATE, false);
        inState.put(IoTValues.CHILLER_STATE, false);
        inState.put(IoTValues.HVAC_MODE, "Chiller");
        inState.put(IoTValues.AWAY_TIMER, false);

        //Meaningful States
        inState.put(IoTValues.PROXIMITY_STATE, true);
        inState.put(IoTValues.ALARM_PASSCODE, "quit");
        inState.put(IoTValues.GIVEN_PASSCODE, "exit");
        inState.put(IoTValues.ALARM_STATE, false);
        inState.put(IoTValues.ALARM_ACTIVE, true);

        StringBuffer sb = new StringBuffer();

        Map<String, Object> outState = new StaticTartanStateEvaluator().evaluateState(inState, sb);

        assertEquals(true, outState.get(IoTValues.ALARM_ACTIVE));
    }

    @Test
    public void testRule9SlightlyDifferentPasscode() {
        Map<String, Object> inState = new HashMap<>();
        //Placeholder states
        inState.put(IoTValues.TEMP_READING, 30);
        inState.put(IoTValues.HUMIDITY_READING, 30);
        inState.put(IoTValues.TARGET_TEMP, 30);
        inState.put(IoTValues.HUMIDIFIER_STATE, false);
        inState.put(IoTValues.DOOR_STATE, false);
        inState.put(IoTValues.LIGHT_STATE, false);
        inState.put(IoTValues.HEATER_STATE, false);
        inState.put(IoTValues.CHILLER_STATE, false);
        inState.put(IoTValues.HVAC_MODE, "Chiller");
        inState.put(IoTValues.AWAY_TIMER, false);

        //Meaningful States
        inState.put(IoTValues.PROXIMITY_STATE, true);
        inState.put(IoTValues.ALARM_PASSCODE, "quit");
        inState.put(IoTValues.GIVEN_PASSCODE, "1exit");
        inState.put(IoTValues.ALARM_STATE, false);
        inState.put(IoTValues.ALARM_ACTIVE, true);

        StringBuffer sb = new StringBuffer();

        Map<String, Object> outState = new StaticTartanStateEvaluator().evaluateState(inState, sb);

        assertEquals(true, outState.get(IoTValues.ALARM_ACTIVE));
    }
}