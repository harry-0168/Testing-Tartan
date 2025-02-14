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
    "lockIntruderSensorMode": lockIntruderSensorMode
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

def test_electronicOperation_case1():
    ''' Input : electronicOperation=on, lockGivenPasscode=passcode, lockRequest=UNLOCK, door=open
        Expected Output : electronicOperation=on, doorLock=unlock, door=closed
    '''
    data = {
        "electronicOperation": "on",
        "lockGivenPasscode": "passcode",
        "lockRequest": "UNLOCK",
        "door": "open",
    }
    response=requests.post(post_url+ houseName, json=data, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    response = requests.get(get_url+ houseName, auth=auth, headers=headers)
    assert response.status_code == 200  # Check HTTP status code
    json_response=response.json()
    assert json_response["tartanHome"]["electronicOperation"]=="on"
    assert json_response["tartanHome"]["doorLock"]=="unlock"
    assert json_response["tartanHome"]["door"]=="closed"

def test_TargetTemperatureChange():
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





