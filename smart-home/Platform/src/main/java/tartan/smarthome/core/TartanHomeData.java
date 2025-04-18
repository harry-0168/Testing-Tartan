package tartan.smarthome.core;

import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Represents a database table for home status
 */
@Entity
@Table(name = "Home")
public class TartanHomeData {

    // Primary key for the table. Not meant to be used
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // the creation time
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", updatable = false)
    private Date createTimeStamp;

    @Column(name = "home_name", nullable = false)
    private String homeName;

    @Column(name = "address", nullable = false)
    private String address;

    // The desired temperature
    @Column(name = "target_temp")
    private String targetTemp;

    // the night start time
    @Column(name = "night_start_time")
    private String nightStartTime;

    // the night end time
    @Column(name = "night_end_time")
    private String nightEndTime;

    // the current temperature
    @Column(name = "temperature")
    private String temperature;

    // the current humidity
    @Column(name = "humidity")
    private String humidity;

    // the state of the door (true if open, false if closed)
    @Column(name = "door_state")
    private String door;

    // the state of the light (true if on, false if off)
    @Column(name = "light_state")
    private String light;

    // the humidifier state (true if on, false if off)
    @Column(name = "humidifier_state")
    private String humidifier;

    // the state of the proximity sensor (true of address occupied, false if vacant)
    @Column(name = "proximity_state")
    private String proximity;

    // the state of the smart door lock (true if locked, false if unlocked)
    @Column(name = "door_lock_state")
    private String doorLock;

    // the state of the arrival proximity sensor (true if someone is arriving, false if no one is arriving)
    @Column(name = "arriving_proximity_state")
    private String arrivingProximity;

    // the state of the keyless entry system (true if enabled, false if disabled)
    @Column(name = "key_less_entry")
    private String keyLessEntry;    

    // the state of the electronic operation (true if enabled, false if disabled)
    @Column(name = "electronic_operation")
    private String electronicOperation;

    // the state of the night lock enabled (true if enabled, false if disabled)
    @Column(name = "night_lock")
    private String nightLock;

    // the state of the lock intruder sensor mode (true if enabled, false if disabled)
    @Column(name = "lock_intruder_sensor")
    private String lockIntruderSensor;

    // the state of the intruder defense sensor mode (true if detected, false if not detected)
    @Column(name = "intruder_defense_sensor")
    private String intruderDefenseSensor;

    // the state of the panel message (true if all clear, false if not all clear)
    @Column(name = "panel_message")
    private String panelMessage;

    // the heater state (true if on, false if off)
    @Column(name = "hvac_mode")
    
    private String hvacMode;
    // The state of the HVAC system
    @Column(name = "hvac_state")
    private String hvacState;

    // the alarm active state (true if alarm sounding, false if alarm not sounding)
    @Column(name = "alarm_active_state")
    private String alarmActive;

    // the alarm delay timeout
    @Column(name = "alarm_delay")
    private String alarmDelay;

    // the alarm enabled state
    @Column(name = "alarm_enabled_state")
    private String alarmArmed;

    @Column(name = "groupExperiment")
    private String groupExperiment;

    @Column(name = "minutes_lights_on")
    private Long minutesLightsOn;

    /**
     * Create a mew data set from a TartanHome model
     * @param h the home model
     */
    public TartanHomeData(TartanHome h) {
        this.homeName = h.getName();
        this.address = h.getAddress();
        this.targetTemp = h.getTargetTemp();
        this.nightStartTime = h.getNightStartTime();
        this.nightEndTime = h.getNightEndTime();
        this.temperature = h.getTemperature();
        this.humidity = h.getHumidity();
        this.door = h.getDoor();
        this.light = h.getLight();
        this.humidifier = h.getHumidifier();
        this.proximity = h.getProximity();
        this.hvacMode = h.getHvacMode();
        this.hvacState = h.getHvacState();
        this.alarmActive = h.getAlarmActive();
        this.alarmDelay = h.getAlarmDelay();
        this.alarmArmed = h.getAlarmArmed();
        this.doorLock = h.getDoorLock();
        this.arrivingProximity = h.getArrivingProximity();
        this.keyLessEntry = h.getKeyLessEntry();
        this.electronicOperation = h.getElectronicOperation();
        this.nightLock = h.getLockNightLockEnabled();
        this.lockIntruderSensor = h.getLockIntruderSensorMode();
        this.intruderDefenseSensor = h.getIntruderDetectionSensor();
        this.panelMessage = h.getPanelMessage();
        this.groupExperiment = h.getGroupExperiment();
        this.minutesLightsOn = h.getMinutesLightsOn();
        // Remember when this record is created
        this.createTimeStamp = new Date();
    }

    public TartanHomeData() {}

    /**
     * Get the name
     * @return the name
     */
    public String getHomeName() {
        return homeName;
    }

    /**
     * Set the name
     * @param homeName the new name
     */
    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    /**
     * Get the address
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the address
     * @param address the new address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    public String getGroupExperiment() {
        return groupExperiment;
    }

    public void setGroupExperiment(String groupExperiment) {
        this.groupExperiment = groupExperiment;
    }

    public Long getMinutesLightsOn() {
        return minutesLightsOn;
    }

    public void setMinutesLightsOn(Long minutesLightsOn) {
        this.minutesLightsOn = minutesLightsOn;
    }

    /**
     * Get the target temperature
     * @return the target temperature
     */
    public String getTargetTemp() {
        return targetTemp;
    }

    /**
     * Set the target temperature
     * @param targetTemp the new target temperature
     */
    public void setTargetTemp(String targetTemp) { this.targetTemp = targetTemp; }

    /**
     * Get the night start time
     * @return the night start time
     */
    public String getNightStartTime() {
        return nightStartTime;
    }

    /**
     * Set the night start time
     * @param nightStartTime the new night start time
     */
    public void setNightStartTime(String nightStartTime) {
        this.nightStartTime = nightStartTime;
    }

    /**
     * Get the night end time
     * @return the night end time
     */
    public String getNightEndTime() {
        return nightEndTime;
    }

    /**
     * Set the night end time
     * @param nightEndTime the new night end time
     */
    public void setNightEndTime(String nightEndTime) {
        this.nightEndTime = nightEndTime;
    }

    /**
     * Get the night lock state
     * @return the night lock state
     */
    public String getNightLock() {
        return nightLock;
    }

    /**
     * Set the night lock state
     * @param nightLock the new state
     */
    public void setNightLock(String nightLock) {
        this.nightLock = nightLock;
    }

    /**
     * Get the current temperature
     * @return the temperature
     */
    public String getTemperature() {
        return this.temperature;
    }

    /**
     * Set the temperature
     * @param temperature the new temperature
     */
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    /**
     * Get the humidity
     * @return Current humidity
     */
    public String getHumidity() {
        return this.humidity;
    }

    /**
     * Set the humidity
     * @param humidity the new humidity
     */
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    /**
     * Get the door state
     * @return the door state
     */
    public String getDoor() {
        return this.door;
    }

    /**
     * Set the door state
     * @param door the new door state
     */
    public void setDoor(String door) {
        this.door = door;
    }

    /**
     * Get the light state
     * @return the light state
     */
    public String getLight() {
        return this.light;
    }

    /**
     * Set the light state
     * @param light the new light state
     */
    public void setLight(String light) {
        this.light = light;
    }

    /**
     * Get the dehumidifier state
     * @return the dehumidifier state
     */
    public String getHumidifier() {
        return humidifier;
    }

    /**
     * Set the dehumidifier state
     * @param humidifier the new state
     */
    public void setHumidifier(String humidifier) {
        this.humidifier = humidifier;
    }

    /**
     * Get the motion sensor state
     * @return the motion sensor state
     */
    public String getProximity() {
        return proximity;
    }

    /**
     * Set the motion sensor state
     * @param proximity the new state
     */
    public void setProximity(String proximity) {
        this.proximity = proximity;
    }

    /**
     * Get the door lock state
     * @return the door lock state
     */
    public String getDoorLock() {
        return doorLock;
    }

    /**
     * Set the door lock state
     * @param doorLock the new state
     */
    public void setDoorLock(String doorLock) {
        this.doorLock = doorLock;
    }

    /**
     * Get the arriving proximity state
     * @return the arriving proximity state
     */
    public String getArrivingProximity() {
        return arrivingProximity;
    }

    /**
     * Set the arriving proximity state
     * @param arrivingProximity the new state
     */
    public void setArrivingProximity(String arrivingProximity) {
        this.arrivingProximity = arrivingProximity;
    }

    /**
     * Get the keyless entry state
     * @return the keyless entry state
     */
    public String getKeyLessEntry() {
        return keyLessEntry;
    }

    /**
     * Set the keyless entry state
     * @param keyLessEntry the new state
     */
    public void setKeyLessEntry(String keyLessEntry) {
        this.keyLessEntry = keyLessEntry;
    }

    /**
     * Get the electronic operation state
     * @return the electronic operation state
     */
    public String getElectronicOperation() {
        return electronicOperation;
    }

    /**
     * Set the electronic operation state
     * @param electronicOperation the new state
     */
    public void setElectronicOperation(String electronicOperation) {
        this.electronicOperation = electronicOperation;
    }

    /**
     * Get the lock intruder sensor state
     * @return the lock intruder sensor state
     */
    public String getLockIntruderSensor() {
        return lockIntruderSensor;
    }

    /**
     * Set the lock intruder sensor state
     * @param lockIntruderSensor the new state
     */
    public void setLockIntruderSensor(String lockIntruderSensor) {
        this.lockIntruderSensor = lockIntruderSensor;
    }

    /**
     * Get the intruder defense sensor state
     * @return the intruder defense sensor state
     */
    public String getIntruderDefenseSensor() {
        return intruderDefenseSensor;
    }

    /**
     * Set the intruder defense sensor state
     * @param intruderDefenseSensor the new state
     */
    public void setIntruderDefenseSensor(String intruderDefenseSensor) {
        this.intruderDefenseSensor = intruderDefenseSensor;
    }

    /**
     * Get the panel message state
     * @return the panel message state
     */
    public String getPanelMessage() {
        return panelMessage;
    }

    /**
     * Set the panel message state
     * @param panelMessage the new state
     */
    public void setPanelMessage(String panelMessage) {
        this.panelMessage = panelMessage;
    }

    /**
     * Get the alarm armed state
     * @return the status of the alarm
     */
    public String getAlarmArmed() {
        return alarmArmed;
    }

    /**
     * Arm/Disarm the alarm
     * @param alarmArmed the new state
     */
    public void setAlarmArmed(String alarmArmed) {
        this.alarmArmed = alarmArmed;
    }

    /**
     * Get the HVAC mode
     * @return the HVAC mode
     */
    public String getHvacMode() {
        return hvacMode;
    }

    /**
     * Set the HVAC mode
     * @param hvacMode the new mode
     */
    public void setHvacMode(String hvacMode) {
        this.hvacMode = hvacMode;
    }

    /**
     * Get the alarm active state
     * @return the current state
     */
    public String getAlarmActive() {
        return alarmActive;
    }

    /**
     * Set the alarm active state
     * @param alarmActive the new state
     */
    public void setAlarmActive(String alarmActive) {
        this.alarmActive = alarmActive;
    }

    /**
     * Get the alarm delay
     * @return the current delay
     */
    public String getAlarmDelay() {
        return alarmDelay;
    }

    /**
     * Set the alarm delay
     * @param alarmDelay the new delay
     */
    public void setAlarmDelay(String alarmDelay) {
        this.alarmDelay = alarmDelay;
    }

    /**
     * Get the HVAC state
     * @return the current state
     */
    public String getHvacState() {
        return hvacState;
    }

    /**
     * Set the HVAC state
     * @param hvacState the new state
     */
    public void setHvacState(String hvacState) {
        this.hvacState = hvacState;
    }


    /**
     * Get the ID
     * @return the ID
     */
    public long getId() {
        return id;
    }

    /**
     * Set the ID
     * @param id the new ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the creation time for this record
     * @return the creation time
     */
    public Date getCreateTimeStamp() { return createTimeStamp; }

    /**
     * Set the creation time
     * @param createTimeStamp the new timestamp
     */
    public void setCreateTimeStamp(Date createTimeStamp) { this.createTimeStamp = createTimeStamp; }

    @Override
    public int hashCode() {
        return Objects.hash(id, homeName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TartanHomeData)) {
            return false;
        }
        final TartanHomeData that = (TartanHomeData) o;
        return Objects.equals(this.homeName, that.homeName);
    }

}
