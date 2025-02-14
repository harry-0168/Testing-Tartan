import json
import requests
from requests.auth import HTTPBasicAuth

url = "http://localhost:8080/"
get_url = "http://localhost:8080/smarthome/state/"
post_url = "http://localhost:8080/smarthome/update/"
houseName = "mse"
username = "admin"
password = "1234"
auth = HTTPBasicAuth(username, password)
headers = {
    "Accept": "application/json"
}

'''
Json format for the post request
{
    "door": door,
    "light": light,
    "targetTemp": targetTemp,
    "humidifier": humidifier,
    "alarmArmed": armAlarm,
    "alarmDelay": alarmDelay,
    "alarmPasscode": passcode,
    "arrivingProximity": arrivingHome,
    "keyLessEntry": keylessEntry,
    "electronicOperation": electronicOperation,
    "lockGivenPasscode": lockPasscode,
    "lockRequest": lockAction,
    "intruderDetectionSensor": intruderDetectionSensor,
    "lockIntruderSensorMode": lockIntruderSensorMode,
    "nightStartTime": nightStartTime,
    "nightEndTime": nightEndTime,
    "lockNightLockEnabled": lockNightLockEnabled,
    "currentTime": currentTime
}
'''

'''
Json format for the get request
{
    "tartanHome": {
        "name": "mse",
        "address": "house-mse",
        "targetTemp": "78",
        "temperature": "-1927",
        "humidity": "100",
        "door": "open",
        "light": "on",
        "humidifier": "off",
        "proximity": "occupied",
        "doorLock": "lock",
        "arrivingProximity": "not_arriving",
        "keyLessEntry": "off",
        "electronicOperation": "off",
        "hvacMode": "heat",
        "hvacState": "on",
        "alarmActive": "inactive",
        "alarmDelay": "30",
        "alarmArmed": "disarmed",
        "eventLog": [
            "[Feb 14,2025 00:16]: Connecting\n",
            "[Feb 14,2025 00:16]: Started update monitor\n",
            "[Feb 14,2025 00:18]: Light on\n[Feb 14,2025 00:18]: Door open\n[Feb 14,2025 00:18]: House is occupied\n[Feb 14,2025 00:18]: Alarm disabled\n[Feb 14,2025 00:18]: Turning on heater, target temperature = 70F, current temperature = 65F\n[Feb 14,2025 00:18]: Automatically disabled dehumidifier when running heater\n[Feb 14,2025 00:18]: Electronic operation of lock is disabled\n",
            "[Feb 14,2025 04:06]: Light on\n[Feb 14,2025 04:06]: Door open\n[Feb 14,2025 04:06]: House is occupied\n[Feb 14,2025 04:06]: Alarm disabled\n[Feb 14,2025 04:06]: Turning on heater, target temperature = 78F, current temperature = -1928F\n[Feb 14,2025 04:06]: Automatically disabled dehumidifier when running heater\n[Feb 14,2025 04:06]: Electronic operation of lock is disabled\n"
        ],
        "authenticated": "false",
        "alarmPasscode": null,
        "lockGivenPasscode": null,
        "lockRequest": null
    }
}
'''

def initializeState():
    data = {
        "door": "open",
        "light": "off",
        "targetTemp": "78",
        "humidifier": "off",
        "alarmArmed": "disarmed",
        "alarmDelay": "30",
        "alarmPasscode": "passcode",
        "arrivingProximity": "not_arriving",
        "keyLessEntry": "off",
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "UNLOCK",
        "intruderDetectionSensor": "off",
        "lockIntruderSensorMode": "off",
        "nightStartTime": "2230",
        "nightEndTime": "615",
        "lockNightLockEnabled": "off",
        "currentTime": "2330"
    }
    response = requests.post(post_url + houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code

# Validates that the server returns the expected error code in the "code" field of the JSON response.
def test_wrongUrl():
    response=requests.get(get_url, auth=auth, headers=headers)
    json_response=response.json()
    assert(json_response["code"]==404)
    assert(json_response["message"]=="HTTP 404 Not Found")

def test_get():
    response=requests.get(get_url+ houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response=response.json()
    assert "tartanHome" in json_response


def test_system_case1():
    ''' Input: nightLockMode=on, time=night, lockGivenPasscode=passcode, lockRequest=UNLOCK, door=open, door=closed
        Expected Output: doorLock=lock
    '''
    initializeState()
    data = {
        "lockNightLockEnabled": "on",
        "nightStartTime": "2230",
        "nightEndTime": "615",
        "currentTime": "2330",
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "UNLOCK",
    }
    response = requests.post(post_url + houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code

    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response = response.json()
    assert json_response["tartanHome"]["doorLock"] == "unlock"

def test_integration_case2():
    """
    Scenario:
      1) Night lock mode is on and time = 2330 (night).
      2) The user unlocks using a correct passcode and 'UNLOCK' request -> door becomes unlocked/open.
      3) The user leaves (house vacant) at time = 2331 -> the system auto-closes and auto-locks the door.
    """

    initializeState()
    test_electronicOperation_lock() # lock and close the door first
    data_step1 = {
        "lockNightLockEnabled": "on",
        "currentTime": "2330",
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "UNLOCK"
    }

    response = requests.post(post_url + houseName, json=data_step1, auth=auth, headers=headers)
    assert response.status_code == 200

    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()

    assert json_response["tartanHome"]["doorLock"] == "unlock", \
        "Door lock should be unlocked with the correct passcode"
    assert json_response["tartanHome"]["door"] == "open", \
        "Door should be opened once unlocked"

    data_step2 = {
        "currentTime": "2331",
        "lockGivenPasscode": "",
        "lockRequest": "noaction"
    }

    response = requests.post(post_url + houseName, json=data_step2, auth=auth, headers=headers)
    assert response.status_code == 200

    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()

    assert json_response["tartanHome"]["door"] == "closed", \
        "Door should be closed since house is vacant"
    assert json_response["tartanHome"]["doorLock"] == "lock", \
        "Door should be locked automatically"

def test_integration_case3():
    """
    Scenario:
      1) The system starts with the door locked (door=closed, doorLock=lock), nightLockEnabled=on,
         currentTime=2340, keylessEntry=on, occupant is arriving.
         => The system auto-unlocks and opens the door.
      2) At time=2341, occupant is still present, so system auto-closes and locks the door again.
    """
    initializeState()
    test_electronicOperation_lock()  # lock and close the door first

    data_step1 = {
        "lockNightLockEnabled": "on",
        "currentTime": "2340",
        "keyLessEntry": "on",
        "arrivingProximity": "arriving",
        "lockGivenPasscode": "",
        "lockRequest": "noaction",
        "electronicOperation": "off"
    }
    response = requests.post(post_url + houseName, json=data_step1, auth=auth, headers=headers)
    assert response.status_code == 200

    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()

    assert json_response["tartanHome"]["doorLock"] == "unlock"
    assert json_response["tartanHome"]["door"] == "open"

    data_step2 = {
        "currentTime": "2341",
        "arrivingProximity": "not_arriving",
        "door": "open",
        "doorLock": "unlock",
        "lockGivenPasscode": "",
        "lockRequest": "noaction"
    }
    response = requests.post(post_url + houseName, json=data_step2, auth=auth, headers=headers)
    assert response.status_code == 200

    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()

    assert json_response["tartanHome"]["door"] == "closed"
    assert json_response["tartanHome"]["doorLock"] == "lock"

# The user unlocks the door with keyless entry and entered, leaved the door open. Then he uses electronic operation to lock and close the door
def test_integration_case4():
    initializeState()
    data_step1 = {
        "currentTime": "1200",
        "arrivingProximity": "arriving",
        "keyLessEntry": "on",
        "door": "closed",
        "lockGivenPasscode": "",
        "lockRequest": "noaction"
    }
    response = requests.post(post_url + houseName, json=data_step1, auth=auth, headers=headers)
    assert response.status_code == 200
    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()
    assert json_response["tartanHome"]["doorLock"] == "unlock"
    assert json_response["tartanHome"]["door"] == "open"

    data_step2 = {
        "lockRequest": "LOCK",
        "lockGivenPasscode": "passcode",
        "electronicOperation": "on"
    }
    response = requests.post(post_url + houseName, json=data_step2, auth=auth, headers=headers)
    assert response.status_code == 200
    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()
    assert json_response["tartanHome"]["door"] == "closed"
    assert json_response["tartanHome"]["doorLock"] == "lock"

# When the night lock mode is off and the door was originally locked and closed. The user uses a correct passcode to unlock and enter and leave the door wide open.
# Later when it gets to the night, the door closed and locked itself.
def test_integration_case6():

    initializeState()
    data_step1 = {
        "door": "closed",
        "doorLock": "lock",
        "lockNightLockEnabled": "on",
        "currentTime": "2215",
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "UNLOCK"
    }

    response = requests.post(post_url + houseName, json=data_step1, auth=auth, headers=headers)
    assert response.status_code == 200

    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()

    assert json_response["tartanHome"]["doorLock"] == "unlock", \
        "Door lock should be unlocked with the correct passcode"
    assert json_response["tartanHome"]["door"] == "open", \
        "Door should be opened once unlocked"

    data_step2 = {
        "currentTime": "2230",
        "lockGivenPasscode": "",
        "lockRequest": "noaction"
    }

    response = requests.post(post_url + houseName, json=data_step2, auth=auth, headers=headers)
    assert response.status_code == 200

    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200
    json_response = response.json()

    assert json_response["tartanHome"]["door"] == "closed", \
        "Door should be closed since house is vacant"
    assert json_response["tartanHome"]["doorLock"] == "lock", \
        "Door should be locked automatically"

def test_system_case8():
    """
    Input:
        LOCK_STATE=True, DOOR_STATE=False, PROXIMITY_STATE=True,
        LOCK_INTRUDER_SENSOR_MODE=True, INTRUDER_DETECTION_SENSOR=True
    Expected Output:
        Initially: LOCK_STATE=False, DOOR_STATE=True (People inside, intruder detected)
        After Proximity=False: LOCK_STATE=True, DOOR_STATE=False (Intruder detection locks door)
        After Unlock Request: LOCK_STATE=True, DOOR_STATE=False (Intruder detection prevents unlock)
        After Keyless Entry: LOCK_STATE=True, DOOR_STATE=False (Still locked)
    """
    initializeState()
    # Step 1: Initialize State
    data = {
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "LOCK",
        "lockIntruderSensorMode": "on",
        "intruderDetectionSensor": "on"
    }
    response = requests.post(post_url+houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200

    # Step 2: Check Initial State
    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response = response.json()
    assert json_response["tartanHome"]["doorLock"] == "unlock", "Door should remain unlocked with people inside during intruder detection"
    assert json_response["tartanHome"]["door"] == "open", "Door should remain open with people inside during intruder detection"

def test_system_case7():
    initializeState()
    # Step 1: Initialize State
    data = {
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "UNLOCK",
        "lockIntruderSensorMode": "on",
        "intruderDetectionSensor": "on"
    }
    response = requests.post(post_url+houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200

    # Step 2: Check Initial State
    response = requests.get(get_url + houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response = response.json()
    assert json_response["tartanHome"]["doorLock"] == "unlock", "Door should remain unlocked with people inside during intruder detection"
    assert json_response["tartanHome"]["door"] == "open", "Door should remain open with people inside during intruder detection"



def test_electronicOperation_lock():
    ''' Input : electronicOperation=on, lockGivenPasscode=passcode, lockRequest=UNLOCK, door=open
        Expected Output : electronicOperation=on, doorLock=unlock, door=closed
    '''
    initializeState()
    data = {
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "LOCK",
        "door": "open",
    }
    response=requests.post(post_url+ houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    response = requests.get(get_url+ houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response=response.json()
    assert json_response["tartanHome"]["electronicOperation"]=="on"
    assert json_response["tartanHome"]["doorLock"]=="lock"
    assert json_response["tartanHome"]["door"]=="closed"



def test_TargetTemperatureChange():
    initializeState()
    data = {
        "targetTemp": "87",
        "door": "open",
        "light": "on",
        "humidifier": "off",
        "keyLessEntry": "off",
        "electronicOperation": "off",
        "alarmActive": "inactive",
        "alarmDelay": "30",
        "alarmArmed": "disarmed",
        "lockRequest": ""
    }
    response=requests.post(post_url+ houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code

    response = requests.get(get_url+ houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response=response.json()
    assert json_response["tartanHome"]["targetTemp"]=="87"

def test_DoorChange():
    initializeState()
    data = {
        "targetTemp": "87",
        "door": "open",
        "light": "on",
        "humidifier": "off",
        "keyLessEntry": "off",
        "electronicOperation": "off",
        "alarmActive": "inactive",
        "alarmDelay": "30",
        "alarmArmed": "disarmed",
        "lockRequest": ""
    }
    response=requests.post(post_url+ houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code

    response = requests.get(get_url+ houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response=response.json()
    assert json_response["tartanHome"]["door"]=="open"

def test_LightChange():
    initializeState()
    data = {
        "light": "off",
        "keyLessEntry": "off",
        "lockRequest": ""
    }
    response=requests.post(post_url+ houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code

    response = requests.get(get_url+ houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response=response.json()
    assert json_response["tartanHome"]["light"]=="on"





