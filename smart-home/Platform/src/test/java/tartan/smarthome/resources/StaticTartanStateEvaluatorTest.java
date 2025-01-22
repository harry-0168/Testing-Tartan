package tartan.smarthome.resources;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import tartan.smarthome.resources.iotcontroller.IoTValues;

public class StaticTartanStateEvaluatorTest {
    @Test
    public void test_rule9() {
        //Same passcode, should disable alarm
        Map<String, Object> inState1 = getObjectMap1();

        StringBuffer sb = new StringBuffer();
        Map<String, Object> outState1 = new StaticTartanStateEvaluator().evaluateState(inState1, sb);
        assertEquals(false, outState1.get(IoTValues.ALARM_ACTIVE));

        //Different Passcode
        Map<String, Object> inState2 = getObjectMap2();

        StringBuffer sb2 = new StringBuffer();
        Map<String, Object> outState2 = new StaticTartanStateEvaluator().evaluateState(inState2, sb2);
        assertEquals(true, outState2.get(IoTValues.ALARM_ACTIVE));

        //Testing the slightly different passcode
        Map<String, Object> inState3 = getObjectMap3();

        StringBuffer sb3 = new StringBuffer();
        Map<String, Object> outState3 = new StaticTartanStateEvaluator().evaluateState(inState3, sb3);
        assertEquals(true, outState3.get(IoTValues.ALARM_ACTIVE));
    }

    private static Map<String, Object> getObjectMap3() {
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
        return inState3;
    }

    private static Map<String, Object> getObjectMap2() {
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
        return inState2;
    }

    private static Map<String, Object> getObjectMap1() {
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
        return inState1;
    }
}