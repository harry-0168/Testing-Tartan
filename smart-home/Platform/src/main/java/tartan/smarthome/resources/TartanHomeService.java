package tartan.smarthome.resources;

import tartan.smarthome.resources.iotcontroller.IoTControlManager;
import tartan.smarthome.resources.iotcontroller.IoTValues;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tartan.smarthome.TartanHomeSettings;
import tartan.smarthome.core.TartanHome;
import tartan.smarthome.core.TartanHomeData;
import tartan.smarthome.core.TartanHomeValues;
import tartan.smarthome.db.HomeDAO;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MILLIS;

/***
 * The service layer for the Tartan Home System. Additional inputs and control mechanisms should be accessed here.
 * Currently, this is mainly a proxy to make the existing hardware RESTful.
 */
public class TartanHomeService {

    // the controller for the house
    private IoTControlManager controller;

    // a logging system
    private static final Logger LOGGER = LoggerFactory.getLogger(TartanHomeService.class);

    // Home configuration parameters
    private String name;
    private String address;
    private Integer port;
    private String alarmDelay;
    private String alarmPasscode;
    private String lockPasscode;
    private String lockRequest;
    private String lockIntruderSensorMode;
    private String intruderDetectionSensor;
    private String panelMessage;
    private String targetTemp;
    private String nightStartTime;
    private String nightEndTime;
    private String lockNightLockEnabled;
    private String user;
    private String password;
    // AB Testing parameters
    private String groupExperiment;
    private Boolean prevLightState;
    private LocalTime timeLightMinutesUpdated;
    private Long lightsOnDuration;

    // status parameters
    private HomeDAO homeDAO;
    private boolean authenticated;

    // historian parameters
    private Boolean logHistory;
    private int historyTimer = 60000;

    /**
     * Create a new Tartan Home Service
     * @param dao handle to a database
     */
    public TartanHomeService(HomeDAO dao) {
        this.homeDAO = dao;
    }

    /**
     * Initialize the settings
     * @param settings the house settings
     * @param historyTimer historian delay
     */
    public void initializeSettings(TartanHomeSettings settings, Integer historyTimer) {

        this.user = settings.getUser();
        this.password = settings.getPassword();
        this.name = settings.getName();
        this.address = settings.getAddress();
        this.port = settings.getPort();
        this.authenticated = false;

        // User configuration
        this.targetTemp = settings.getTargetTemp();
        this.alarmDelay = settings.getAlarmDelay();
        this.nightStartTime = settings.getNightStartTime();
        this.nightEndTime = settings.getNightEndTime();
        this.alarmPasscode = settings.getAlarmPasscode();
        this.lockPasscode = settings.getLockPasscode();

        this.historyTimer = historyTimer*1000;
        this.logHistory = true;
        // AB Testing - should not be part of DB
        this.groupExperiment = settings.getGroupExperiment();
        // if it is null, use hash to determine group
        if (this.groupExperiment == null) {
            this.groupExperiment = String.valueOf(Math.abs(this.name.hashCode()) % 2);
        }
        this.timeLightMinutesUpdated = LocalTime.now();
        this.lightsOnDuration = 0L;
        this.prevLightState = true;

        // Create and initialize the controller for this house
        this.controller = new IoTControlManager(user, password, new StaticTartanStateEvaluator());
        
        TartanHome temp = new TartanHome();
        temp.setAlarmDelay(alarmDelay);

        Map<String, Object> userSettings = new Hashtable<String, Object>();
        userSettings.put(IoTValues.ALARM_DELAY, Integer.parseInt(this.alarmDelay));
        userSettings.put(IoTValues.TARGET_TEMP, Integer.parseInt(this.targetTemp));
        userSettings.put(IoTValues.NIGHT_START_TIME, Integer.parseInt(this.nightStartTime));
        userSettings.put(IoTValues.NIGHT_END_TIME, Integer.parseInt(this.nightEndTime));
        userSettings.put(IoTValues.ALARM_PASSCODE, this.alarmPasscode);
        userSettings.put(IoTValues.LOCK_PASSCODE, this.lockPasscode);
        controller.updateSettings(userSettings);

        LOGGER.info("House " + this.name + " configured");
    }

    /**
     * Stop logging history
     */
    public void stopHistorian() {
        this.logHistory = false;
    }

    /**
     * Start a thread to log house history on a delay
     */
    public void startHistorian() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (logHistory) {
                    try {
                        TartanHome state = getState();
                        if (state != null) {
                            TartanHomeData home = new TartanHomeData(state);
                            LOGGER.info("Logging " + name + "@" + address + " state");
                            logHistory(home);
                        }

                        Thread.sleep(historyTimer);
                    } catch (Exception x) {
                        LOGGER.error("Failed to save " + name + "@" + address + " state");
                    }
                }
            }
        }).start();
    }

    /**
     * Save the current state of the house
     * @param tartanHomeData the current state in a Hibernate-aware format
     */
    @UnitOfWork
    private void logHistory(TartanHomeData tartanHomeData) {
        homeDAO.create(tartanHomeData);
    }

    /**
     * Get the name for this house
     * @return the house name
     */
    public String getName() {
        return name;
    }

    public Boolean authenticate(String user, String pass) {
        this.authenticated = (this.user.equals(user) && this.password.equals(pass));
        return this.authenticated;
    }

    /**
     * Get the house address
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     *  Get the house conncected state
     * @return true if connected; false otherwise
     */
    public Boolean isConnected() {
        return controller.isConnected();
    }

    /**
     * Convert humidifier state
     * @param tartanHome the home
     *  @return true if on; false if off; otherwise null
     */
    private Boolean toIoTHumdifierState(TartanHome tartanHome) {
        if (tartanHome.getHumidifier().equals(TartanHomeValues.OFF)) return false;
        else if (tartanHome.getHumidifier().equals(TartanHomeValues.ON)) return true;
        return null;
    }

    /**
     * Convert light state
     * @param tartanHome the home
     * @return true if on; false if off; otherwise null
     */
    private Boolean toIoTLightState(TartanHome tartanHome) {
        if (tartanHome.getLight().equals(TartanHomeValues.OFF)) return false;
        else if (tartanHome.getLight().equals(TartanHomeValues.ON)) return true;
        return null;
    }

    /**
     * Convert alarm armed state
     * @param tartanHome the home
     * @return true if armed; false if disarmed; otherwise null
     */
    private Boolean toIoTAlarmArmedState(TartanHome tartanHome) {
        if (tartanHome.getAlarmArmed().equals(TartanHomeValues.DISARMED)) return false;
        else if (tartanHome.getAlarmArmed().equals(TartanHomeValues.ARMED)) return true;
        return null;
    }

    /**
     * Convert alarm delay
     * @param tartanHome the home
     * @return the converted delay
     */
    private Integer toIoTAlarmDelay(TartanHome tartanHome) {
        return Integer.parseInt(tartanHome.getAlarmDelay());
    }

    /**
     * Convert alarm passcode
     * @param tartanHome the home
     * @return the passcode
     */
    private String toIoTPasscode(TartanHome tartanHome) {
        return tartanHome.getAlarmPasscode();
    }

    /**
     * Convert lock passcode
     * @param tartanHome the home
     * @return the passcode
     */
    private String toIoTLockPasscode(TartanHome tartanHome) {
        return tartanHome.getLockGivenPasscode();
    }

    /*
     * Convert current time
     * @param tartanHome the home
     * @return the time
     */
    private Integer toIoTCurrentTime(TartanHome tartanHome) {
        return Integer.parseInt(tartanHome.getCurrentTime());
    }

    /**
     * Convert lock request
     * @param tartanHome
     * @return the request (LOCK/UNLOCK/noaction)
     */
    private String toIoTLockRequest(TartanHome tartanHome) {
        return tartanHome.getLockRequest();
    }

    /*
     * Convert Lock Intruder Sensor Mode
     * @param tartanHome
     * @return the mode (ON/OFF)
     */
    private Boolean toIoTLockIntruderSensorMode(TartanHome tartanHome) {
        if (tartanHome.getLockIntruderSensorMode().equals(TartanHomeValues.ON)) return true;
        else if (tartanHome.getLockIntruderSensorMode().equals(TartanHomeValues.OFF)) return false;
        return null;
    }

    /**
     * Convert Intruder Detection Sensor
     * @param tartanHome
     * @return the sensor state (ON/OFF)
     */
    private Boolean toIoTIntruderDetectionSensor(TartanHome tartanHome) {
        if (tartanHome.getIntruderDetectionSensor().equals(TartanHomeValues.ON)) return true;
        else if (tartanHome.getIntruderDetectionSensor().equals(TartanHomeValues.OFF)) return false;
        return null;
    }

    /*
     * Convert night lock enabled state
     * @param tartanHome
     * @return the state (ON/OFF)
     */
    private Boolean toIoTNightLockEnabled(TartanHome tartanHome) {
        if (tartanHome.getLockNightLockEnabled().equals(TartanHomeValues.ON)) return true;
        else if (tartanHome.getLockNightLockEnabled().equals(TartanHomeValues.OFF)) return false;
        return null;
    }

    /**
     * Convert Panel Message
     * @param tartanHome
     * @return the message
     */
    private String toIoTPanelMessage(TartanHome tartanHome) {
        return tartanHome.getPanelMessage();
    }

    /**
     * Convert door state
     * @param tartanHome the home
     * @return true if open; false if closed' otherwise null
     */
    private Boolean toIoTDoorState(TartanHome tartanHome) {
        if (tartanHome.getDoor().equals(TartanHomeValues.CLOSED)) return false;
        else if (tartanHome.getDoor().equals(TartanHomeValues.OPEN)) return true;
        return null;
    }

    /**
     * Convert proximity state
     * @param tartanHome the home
     * @return true if occupied; false if empty; otherwise null
     */
    private Boolean toIoTProximityState(TartanHome tartanHome) {
        if (tartanHome.getProximity().equals(TartanHomeValues.OCCUPIED)) return true;
        else if (tartanHome.getProximity().equals(TartanHomeValues.EMPTY)) return false;
        return null;
    }

    /**
     * Convert smart door lock state
     * @param tartanHome the home
     * @return true if locked; false if unlocked; otherwise null
     */
    private Boolean toIoTLockState(TartanHome tartanHome) {
        if (tartanHome.getDoorLock().equals(TartanHomeValues.LOCK)) return true;
        else if (tartanHome.getDoorLock().equals(TartanHomeValues.UNLOCK)) return false;
        return null;
    }

    /**
     * Convert arriving proximity state
     * @param tartanHome
     * @return true if arriving; false if not arriving; otherwise null
     */
    private Boolean toIoTArrivingProximityState(TartanHome tartanHome) {
        if (tartanHome.getArrivingProximity().equals(TartanHomeValues.ARRIVING)) return true;
        else if (tartanHome.getArrivingProximity().equals(TartanHomeValues.NOT_ARRIVING)) return false;
        return null;
    }

    /**
     * Convert Keyless entry state
     * @param tartanHome
     * @return true if keyless entry is enabled; false if disabled; otherwise null
     */
    private Boolean toIoTKeylessEntryState(TartanHome tartanHome) {
        if (tartanHome.getKeyLessEntry().equals(TartanHomeValues.ON)) return true;
        else if (tartanHome.getKeyLessEntry().equals(TartanHomeValues.OFF)) return false;
        return null;
    }

    /**
     * Convert Electronic operation state
     * @param tartanHome
     * @return
     */
    private Boolean toIoTElectronicOperationState(TartanHome tartanHome) {
        if (tartanHome.getElectronicOperation().equals(TartanHomeValues.ON)) return true;
        else if (tartanHome.getElectronicOperation().equals(TartanHomeValues.OFF)) return false;
        return null;
    }

    /**
     * Convert alarm active state
     * @param tartanHome the home
     * @return true if active; false if inactive; otherwise null
     */
    private Boolean toIoTAlarmActiveState(TartanHome tartanHome) {
        if (tartanHome.getAlarmActive().equals(TartanHomeValues.ACTIVE)) return true;
        else if (tartanHome.getAlarmActive().equals(TartanHomeValues.INACTIVE)) return false;
        return null;
    }

    /**
     * Convert heater state
     * @param tartanHome the home
     * @return true if on; false if off; otherwise null
     */
    private Boolean toIoTHeaterState(TartanHome tartanHome) {
        if (tartanHome.getHvacMode().equals(TartanHomeValues.HEAT)) {
            if (tartanHome.getHvacState().equals(TartanHomeValues.ON)) {
                return true;
            } else if (tartanHome.getHvacState().equals(TartanHomeValues.OFF)) {
                return false;
            }
        }
        return null;
    }

    /**
     * Convert chiller state
     * @param tartanHome the home
     * @return true if on; false if off; otherwise null
     */
    private Boolean toIoTChillerState(TartanHome tartanHome) {
        if (tartanHome.getHvacMode().equals(TartanHomeValues.COOL)) {
            if (tartanHome.getHvacState().equals(TartanHomeValues.ON)) {
                return true;
            } else if (tartanHome.getHvacState().equals(TartanHomeValues.OFF)) {
                return false;
            }
        }
        return null;
    }

    /**
     * Convert target temperature state
     * @param tartanHome the home
     * @return converted target temperature
     */
    private Integer toIoTTargetTempState(TartanHome tartanHome) {
        return Integer.parseInt(tartanHome.getTargetTemp());
    }

    /*
     * Convert night start time
     * @param tartanHome the home
     * @return the start time
     */
    private Integer toIoTNightStartTime(TartanHome tartanHome) {
        return Integer.parseInt(tartanHome.getNightStartTime());
    }

    /**
     * Convert night end time
     * @param tartanHome the home
     * @return the end time
     */
    private Integer toIoTNightEndTime(TartanHome tartanHome) {
        return Integer.parseInt(tartanHome.getNightEndTime());
    }

    /**
     * Convert HVAC mode state
     * @param tartanHome the home
     * @return Heater, Chiller; or null
     */
    private String toIoTHvacModeState(TartanHome tartanHome) {
        if (tartanHome.getHvacMode().equals(TartanHomeValues.HEAT)) return "Heater";
        else if (tartanHome.getHvacMode().equals(TartanHomeValues.COOL)) return "Chiller";
        return null;
    }

    /**
     * Set the house state in the hardware
     * @param h the new state
     * @return true
     */
    public Boolean setState(TartanHome h) {
        synchronized (controller) {
                        
            Map<String, Object> userSettings = new Hashtable<String, Object>();
            if (h.getAlarmDelay()!=null) {
                this.alarmDelay = h.getAlarmDelay();
                userSettings.put(IoTValues.ALARM_DELAY, Integer.parseInt(this.alarmDelay)); 

            }
            if (h.getTargetTemp()!=null) {
                this.targetTemp = h.getTargetTemp();
                userSettings.put(IoTValues.TARGET_TEMP, Integer.parseInt(this.targetTemp)); 
            }           
            if (h.getNightStartTime()!=null) {
                this.nightStartTime = h.getNightStartTime();
                userSettings.put(IoTValues.NIGHT_START_TIME, Integer.parseInt(this.nightStartTime)); 
            }
            if (h.getNightEndTime()!=null) {
                this.nightEndTime = h.getNightEndTime();
                userSettings.put(IoTValues.NIGHT_END_TIME, Integer.parseInt(this.nightEndTime)); 
            }
            controller.updateSettings(userSettings);  
            controller.processStateUpdate(toIotState(h));  
        }
        return true;
    }

    /**
     * Fetch the current state of the house
     * @return the current state
     */
    public TartanHome getState() {

        TartanHome tartanHome = new TartanHome();

        tartanHome.setName(this.name);
        tartanHome.setAddress(this.address);

        tartanHome.setTargetTemp(this.targetTemp);
        tartanHome.setNightStartTime(this.nightStartTime);
        tartanHome.setNightEndTime(this.nightEndTime);
        tartanHome.setAlarmDelay(this.alarmDelay);

        tartanHome.setEventLog(controller.getLogMessages());
        tartanHome.setAuthenticated(String.valueOf(this.authenticated));
        tartanHome.setGroupExperiment(this.groupExperiment);

        Map<String, Object> state = null;
        synchronized (controller) {
            state = controller.getCurrentState();            
            for (String l : controller.getLogMessages()) {
                LOGGER.info(l);
            }
        }
        if (state == null) {
            LOGGER.info("zUsing default state");
            // There is no state, but something must be returned.

            tartanHome.setTemperature(TartanHomeValues.UNKNOWN);
            tartanHome.setHumidity(TartanHomeValues.UNKNOWN);
            tartanHome.setTargetTemp(TartanHomeValues.UNKNOWN);
            tartanHome.setNightStartTime(TartanHomeValues.UNKNOWN);
            tartanHome.setNightEndTime(TartanHomeValues.UNKNOWN);
            tartanHome.setLockNightLockEnabled(TartanHomeValues.UNKNOWN);
            tartanHome.setHumidifier(TartanHomeValues.UNKNOWN);
            tartanHome.setDoor(TartanHomeValues.UNKNOWN);
            tartanHome.setLight(TartanHomeValues.UNKNOWN);
            tartanHome.setProximity(TartanHomeValues.UNKNOWN);
            tartanHome.setAlarmArmed(TartanHomeValues.UNKNOWN);
            tartanHome.setAlarmActive(TartanHomeValues.UNKNOWN);
            tartanHome.setHvacMode(TartanHomeValues.UNKNOWN);
            tartanHome.setHvacState(TartanHomeValues.UNKNOWN);
            tartanHome.setDoorLock(TartanHomeValues.UNKNOWN);
            tartanHome.setArrivingProximity(TartanHomeValues.UNKNOWN);
            tartanHome.setKeyLessEntry(TartanHomeValues.UNKNOWN);
            tartanHome.setElectronicOperation(TartanHomeValues.UNKNOWN);
            tartanHome.setLockIntruderSensorMode(TartanHomeValues.UNKNOWN);
            tartanHome.setIntruderDetectionSensor(TartanHomeValues.UNKNOWN);
            tartanHome.setPanelMessage(TartanHomeValues.UNKNOWN);
            return tartanHome;
        }

        // A valid state was found, so use it

        Set<String> keys = state.keySet();
        for (String key : keys) {
            LOGGER.info("State element: " + key + "=" + state.get(key));
            if (key.equals(IoTValues.TEMP_READING)) {
                tartanHome.setTemperature(String.valueOf(state.get(key)));
            } else if (key.equals(IoTValues.HUMIDITY_READING)) {
                tartanHome.setHumidity(String.valueOf(state.get(key)));
            }
            else if (key.equals(IoTValues.TARGET_TEMP)) {
                tartanHome.setTargetTemp(String.valueOf(state.get(key)));
            } else if(key.equals(IoTValues.NIGHT_START_TIME)) {
                tartanHome.setNightStartTime(String.valueOf(state.get(key)));
            } else if(key.equals(IoTValues.NIGHT_END_TIME)) {
                tartanHome.setNightEndTime(String.valueOf(state.get(key)));
            } else if (key.equals(IoTValues.HUMIDIFIER_STATE)) {
                Boolean humidifierState = (Boolean)state.get(key);
                if (humidifierState) {
                    tartanHome.setHumidifier(String.valueOf(TartanHomeValues.ON));
                } else {
                    tartanHome.setHumidifier(String.valueOf(TartanHomeValues.OFF));
                }
            } else if (key.equals(IoTValues.DOOR_STATE)) {
                Boolean doorState = (Boolean)state.get(key);
                if (doorState) {
                    tartanHome.setDoor(TartanHomeValues.OPEN);
                } else {
                    tartanHome.setDoor(TartanHomeValues.CLOSED);
                }
            } else if (key.equals(IoTValues.LIGHT_STATE)) {
                Boolean lightState = (Boolean)state.get(key);
                if (lightState) {
                    tartanHome.setLight(TartanHomeValues.ON);
                } else {
                    tartanHome.setLight(TartanHomeValues.OFF);
                }

                boolean currentState = lightState;      // true = on, false = off
                boolean previousState = this.prevLightState;  // same, true/false

                // Light just turned ON
                if (currentState && !previousState) {
                    // record the time we turned on
                    this.timeLightMinutesUpdated = LocalTime.now();
                }
                // Light remains ON
                else if (currentState && previousState) {
                    // accumulate usage
                    LocalTime now = LocalTime.now();
                    Long diff = this.timeLightMinutesUpdated.until(now, ChronoUnit.MILLIS);
                    this.timeLightMinutesUpdated = now;
                    this.lightsOnDuration += diff;
                }
                // Light just turned OFF
                else if (!currentState && previousState) {
                    // do one final accumulation for that on-cycle
                    LocalTime now = LocalTime.now();
                    Long diff = this.timeLightMinutesUpdated.until(now, ChronoUnit.MILLIS);
                    this.timeLightMinutesUpdated = now;
                    this.lightsOnDuration += diff;
                }
                // If it's OFF and stays OFF, do nothing
                
                tartanHome.setMinutesLightsOn(lightsOnDuration);
                this.prevLightState = currentState;

            } else if (key.equals(IoTValues.PROXIMITY_STATE)) {
                Boolean proxState = (Boolean)state.get(key);
                if (proxState) {
                    tartanHome.setProximity(TartanHomeValues.OCCUPIED);
                } else {
                    tartanHome.setProximity(TartanHomeValues.EMPTY);
                }
            } else if (key.equals(IoTValues.LOCK_STATE)) {
                Boolean lockState = (Boolean)state.get(key);
                if (lockState) {
                    tartanHome.setDoorLock(TartanHomeValues.LOCK);
                } else {
                    tartanHome.setDoorLock(TartanHomeValues.UNLOCK);
                }
            } else if (key.equals(IoTValues.ARRIVING_PROXIMITY_STATE)) {
                Boolean arrivingProxState = (Boolean)state.get(key);
                if (arrivingProxState) {
                    tartanHome.setArrivingProximity(TartanHomeValues.ARRIVING);
                } else {
                    tartanHome.setArrivingProximity(TartanHomeValues.NOT_ARRIVING);
                }
            } else if (key.equals(IoTValues.LOCK_KEYLESS_ENTRY_ENABLE)) {
                Boolean keylessEntryState = (Boolean)state.get(key);
                if (keylessEntryState) {
                    tartanHome.setKeyLessEntry(TartanHomeValues.ON);
                } else {
                    tartanHome.setKeyLessEntry(TartanHomeValues.OFF);
                }
            } else if (key.equals(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE)) {
                Boolean electronicOperationState = (Boolean)state.get(key);
                if (electronicOperationState) {
                    tartanHome.setElectronicOperation(TartanHomeValues.ON);
                } else {
                    tartanHome.setElectronicOperation(TartanHomeValues.OFF);
                }
            } else if (key.equals(IoTValues.LOCK_NIGHT_LOCK_ENABLED)) {
                Boolean nightLockEnabled = (Boolean)state.get(key);
                if (nightLockEnabled) {
                    tartanHome.setLockNightLockEnabled(TartanHomeValues.ON);
                } else {
                    tartanHome.setLockNightLockEnabled(TartanHomeValues.OFF);
                }
            } else if (key.equals(IoTValues.LOCK_INTRUDER_SENSOR_MODE)) {
                Boolean lockIntruderSensorMode = (Boolean)state.get(key);
                if (lockIntruderSensorMode) {
                    tartanHome.setLockIntruderSensorMode(TartanHomeValues.ON);
                } else {
                    tartanHome.setLockIntruderSensorMode(TartanHomeValues.OFF);
                }
            } else if (key.equals(IoTValues.INTRUDER_DETECTION_SENSOR)) {
                Boolean intruderDetectionSensor = (Boolean)state.get(key);
                if (intruderDetectionSensor) {
                    tartanHome.setIntruderDetectionSensor(TartanHomeValues.ON);
                } else {
                    tartanHome.setIntruderDetectionSensor(TartanHomeValues.OFF);
                }
            } else if (key.equals(IoTValues.PANEL_MESSAGE)) {
                Boolean panelMessage = (Boolean)state.get(key);
                if (panelMessage) {
                    tartanHome.setPanelMessage(TartanHomeValues.ON);
                } else {
                    tartanHome.setPanelMessage(TartanHomeValues.OFF);
                }
            }
            else if (key.equals(IoTValues.ALARM_STATE)) {
                Boolean alarmState = (Boolean)state.get(key);
                if (alarmState) {
                    tartanHome.setAlarmArmed(TartanHomeValues.ARMED);
                } else {
                    tartanHome.setAlarmArmed(TartanHomeValues.DISARMED);
                }
            }
            else if (key.equals(IoTValues.ALARM_ACTIVE)) {
                Boolean alarmActiveState = (Boolean)state.get(key);
                if (alarmActiveState) {
                    tartanHome.setAlarmActive(TartanHomeValues.ACTIVE);
                } else {
                    tartanHome.setAlarmActive(TartanHomeValues.INACTIVE);
                }

            } else if (key.equals(IoTValues.HVAC_MODE)) {
                if (state.get(key).equals("Heater")) {
                    tartanHome.setHvacMode(TartanHomeValues.HEAT);
                } else if (state.get(key).equals("Chiller")) {
                    tartanHome.setHvacMode(TartanHomeValues.COOL);
                }

                // If either heat or chill is on then the hvac is on
                String heaterState = String.valueOf(state.get(IoTValues.HEATER_STATE));
                String chillerState = String.valueOf(state.get(IoTValues.CHILLER_STATE));

                if (heaterState.equals("true") || chillerState.equals("true")) {
                    tartanHome.setHvacState(TartanHomeValues.ON);

                } else {
                    tartanHome.setHvacState(TartanHomeValues.OFF);
                }
            }
        }
        
        return tartanHome;
    }

    /**
     * Convert the state to a format suitable for the hardware
     * @param tartanHome the state
     * @return a map of settings appropriate for the hardware
     */
    private Map<String, Object> toIotState(TartanHome tartanHome) {
        Map<String, Object> state = new Hashtable<>();
        
        if (tartanHome.getProximity()!=null) {
            state.put(IoTValues.PROXIMITY_STATE, toIoTProximityState(tartanHome));
        }

        if (tartanHome.getDoorLock()!=null) {
            state.put(IoTValues.LOCK_STATE, toIoTLockState(tartanHome));
        }

        if (tartanHome.getArrivingProximity()!=null) {
            state.put(IoTValues.ARRIVING_PROXIMITY_STATE, toIoTArrivingProximityState(tartanHome));
        }

        if (tartanHome.getKeyLessEntry()!=null) {
            state.put(IoTValues.LOCK_KEYLESS_ENTRY_ENABLE, toIoTKeylessEntryState(tartanHome));
        }

        if (tartanHome.getElectronicOperation()!=null) {
            state.put(IoTValues.LOCK_ELECTRONIC_OPERATION_ENABLE, toIoTElectronicOperationState(tartanHome));
        }

        if (tartanHome.getLockNightLockEnabled()!=null) {
            state.put(IoTValues.LOCK_NIGHT_LOCK_ENABLED, toIoTNightLockEnabled(tartanHome));
        }

        if (tartanHome.getLockIntruderSensorMode()!=null) {
            state.put(IoTValues.LOCK_INTRUDER_SENSOR_MODE, toIoTLockIntruderSensorMode(tartanHome));
        }

        if (tartanHome.getIntruderDetectionSensor()!=null) {
            state.put(IoTValues.INTRUDER_DETECTION_SENSOR, toIoTIntruderDetectionSensor(tartanHome));
        }

        if (tartanHome.getPanelMessage()!=null) {
            state.put(IoTValues.PANEL_MESSAGE, toIoTPanelMessage(tartanHome));
        }

        if (tartanHome.getDoor()!=null) {
            state.put(IoTValues.DOOR_STATE, toIoTDoorState(tartanHome));
        }
        if (tartanHome.getLight()!=null) {
            state.put(IoTValues.LIGHT_STATE, toIoTLightState(tartanHome));
        }
        if (tartanHome.getHumidifier()!=null) {
            state.put(IoTValues.HUMIDIFIER_STATE, toIoTHumdifierState(tartanHome));
        }
        if (tartanHome.getAlarmActive()!=null) {
            state.put(IoTValues.ALARM_ACTIVE, toIoTAlarmActiveState(tartanHome));
        }
        // entering a passcode also disables the alarm
        if (tartanHome.getAlarmPasscode()!=null) {
            state.put(IoTValues.GIVEN_PASSCODE, toIoTPasscode(tartanHome));
            tartanHome.setAlarmArmed(TartanHomeValues.DISARMED);
            state.put(IoTValues.ALARM_STATE, toIoTAlarmArmedState(tartanHome));
        }
        else {
            if (tartanHome.getAlarmArmed() != null) {
                state.put(IoTValues.ALARM_STATE, toIoTAlarmArmedState(tartanHome));
            }
        }
        if (tartanHome.getLockGivenPasscode()!=null) {
            state.put(IoTValues.LOCK_GIVEN_PASSCODE, toIoTLockPasscode(tartanHome));
        }
        if (tartanHome.getCurrentTime()!=null) {
            state.put(IoTValues.CURRENT_TIME, toIoTCurrentTime(tartanHome));
        }
        if (tartanHome.getLockRequest()!=null) {
            state.put(IoTValues.LOCK_REQUEST, toIoTLockRequest(tartanHome));
        }
        if (tartanHome.getAlarmDelay()!=null) {
            this.alarmDelay = tartanHome.getAlarmDelay();

            Hashtable<String, Object> ht = new Hashtable<String, Object>(){
                {put(IoTValues.ALARM_DELAY,Integer.parseInt(TartanHomeService.this.alarmDelay));}
            };
            controller.updateSettings(ht);
        }

        if (tartanHome.getHvacMode()!=null) {
            if (tartanHome.getHvacMode().equals(TartanHomeValues.HEAT)) {
                state.put(IoTValues.HVAC_MODE, "Heater");
                if (tartanHome.getHvacState()!=null) {
                    state.put(IoTValues.HEATER_STATE, toIoTHeaterState(tartanHome));
                }
            }
            if (tartanHome.getHvacMode().equals(TartanHomeValues.COOL)) {
                state.put(IoTValues.HVAC_MODE, "Chiller");
                if (tartanHome.getHvacState()!=null) {
                    if (tartanHome.getHvacState().equals(TartanHomeValues.ON)) {
                        state.put(IoTValues.CHILLER_ON, toIoTChillerState(tartanHome));
                    }
                }
            }
        }
        
        for (Map.Entry<String,Object> e : state.entrySet()) {
            LOGGER.info("State: " + e.getKey() + "=" + e.getValue());
        }

        return state;
    }

    /**
     * Connect to the house
     * @throws TartanHomeConnectException exception passed when connect fails
     */
    public void connect() throws TartanHomeConnectException {
        if (controller.isConnected() == false) {
            if (!controller.connectToHouse(this.address, this.port, this.user, this.password)) {
                throw new TartanHomeConnectException();
            }
        }
    }
}