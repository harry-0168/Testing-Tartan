package tartan.smarthome.resources.iotcontroller;

/**
 * Constant values used in the house
 *
 * Project: LG Exec Ed Program
 * Copyright: Copyright (c) 2015 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2015 - initial version
 */
public abstract class IoTValues {

    // state readings
    public static final String TEMP_READING = "TR";
    public static final String HUMIDITY_READING = "HR";
    public static final String HUMIDIFIER_STATE = "HUS";
    public static final String DOOR_STATE = "DS";
    public static final String LIGHT_STATE = "LS";
    public static final String PROXIMITY_STATE = "PS";
    public static final String ARRIVING_PROXIMITY_STATE = "APS";
    public static final String ALARM_STATE = "AS";
    public static final String HVAC_MODE = "HM";
    public static final String ALARM_ACTIVE = "AA";
    public static final String HEATER_STATE = "HES";
    public static final String CHILLER_STATE = "CHS";
   
    // protocol control values
    public static final String PARAM_DELIM = ";";
    public static final String MSG_DELIM = ":";
    public static final String PARAM_EQ = "=";
    public static final String MSG_END = ".";

    // target temperature
    public static final String TARGET_TEMP = "TT";

    public static final String DOOR_CLOSE = "0";
    public static final String DOOR_OPEN = "1";

    public static final String UNLOCK = "0";
    public static final String LOCK = "1";

    public static final String LIGHT_ON = "1";
    public static final String LIGHT_OFF = "0";

    public static final String HUMIDIFIER_ON = "1";
    public static final String HUMIDIFIER_OFF = "0";

    public static final String ALARM_ENABLED  = "1";
    public static final String ALARM_DISABLED = "0";

    public static final String ALARM_ON = "1";
    public static final String ALARM_OFF = "0";

    public static final String HEATER_ON = "1";
    public static final String HEATER_OFF = "0";

    public static final String CHILLER_ON = "1";
    public static final String CHILLER_OFF = "0";

    public static final String OK = "OK";

    public static final String ALARM_DELAY = "ALARM_DELAY";
    public static final String ALARM_PASSCODE = "ALARM_PASSCODE";
    public static final String GIVEN_PASSCODE = "GIVEN_PASSCODE";

    public static final String GET_STATE = "GS";
    public static final String SET_STATE = "SS";
    public static final String STATE_UPDATE = "SU";

    public static final String SETTINGS_FILE = "settings.txt";
    public static final String USERS_DB = "users.txt";
	  public static final String AWAY_TIMER = "AW";

    public static final String LOCK_STATE = "LKS";
    public static final String LOCK_REQUEST = "LOCK_REQUEST";
    public static final String LOCK_ELECTRONIC_OPERATION_ENABLE = "EOE";
    public static final String LOCK_KEYLESS_ENTRY_ENABLE = "KLE";
    public static final String LOCK_PASSCODE = "LOCK_PASSCODE";
    public static final String LOCK_GIVEN_PASSCODE = "LOCK_GIVEN_PASSCODE";

    public static final String LOCK_NIGHT_LOCK_ENABLED = "NLE";
    public static final String NIGHT_START_TIME = "NST";
    public static final String NIGHT_END_TIME = "NET";
    public static final String CURRENT_TIME = "CT";

    public static final String LOCK_INTRUDER_SENSOR_MODE = "LIS";
    public static final String INTRUDER_DETECTION_SENSOR = "IDS";
    public static final String PANEL_MESSAGE = "PM";
}
