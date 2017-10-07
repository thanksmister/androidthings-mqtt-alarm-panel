# Android Things Alarm Panel for Home Assistant

This project is a MQTT Alarm Control Panel for use with [Home Assistant's Manual Alarm Control Panel](https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) component. However, the application can be used with any home automation platform that supports the MQTT messaging protocol such as OpenHab, Node-RED, or a cloud-based MQTT services. This project was built using a Raspberry Pi 3 and a 7" Touchscreen display running Android Things.

- Alarm Panel Video: https://youtu.be/xspCZoRIBNQ
- Instructions to build your own: https://www.hackster.io/thanksmister/mqtt-alarm-control-panel-for-home-assistant-a206cc
- Android Tablet: https://github.com/thanksmister/android-mqtt-alarm-panel

The alarm control panel acts as an interface for your own home alarm system and allows for two way communication using MQTT messaging.  You can set the alarm state to away or home, or disarm the alarm using a code.  Your home automation system will controls the sensors or automation that triggers the siren or notifies users of possible intrusion.  

To use the application with Home Assistant, you need to use the [Home Assistant's Manual Alarm Control Panel](https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) component and have the MQTT service setup and configured. In the alarm control panel application you enter your MQTT broker address, port number, and credentials if necessary.   If you are using an automation system other than Home Assistant, you will need to use the same publish states and commands in your setup.

# Default Command and Publish States

- Command topic:  home/alarm/set
- Command payloads: ARM_HOME, ARM_AWAY, DISARM
- Publish topic: home/alarm
- Publish payloads: disarmed, armed_away, armed_home, pending, triggered.

![alarm_home](https://user-images.githubusercontent.com/142340/29889460-9f615642-8d9a-11e7-99a6-1a49529dd580.png)

# Hardware

- Raspberry Pi 3 and SD Card.
- [7" Touchscreen Display for display](https://www.adafruit.com/product/2718).
- [Piezo Buzzer](https://www.adafruit.com/product/160) for button feedback and sounds.

# Software

- [Android Things 0.4.1-devpreview for Raspbery Pi 3](https://developer.android.com/things/hardware/raspberrypi.html)
- Android Studio with Android SDK N or above.

# Home Assistant Setup

- Setup [Home Assistant](https://home-assistant.io/getting-started/)
- Configure the [MQTT service](https://home-assistant.io/components/mqtt/) note the broker address, port and username/password if applicable.
- Add the [MQTT Alarm Control Panel] (https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) to your configuraiton with the default settings for now.
- Add any sensors (like Zwave door sensors or sirens) and configure automations to trigger the alarm.

# Raspberry PI Setup

Make sure you properly setup the RPi3 with the 7" Touchscreen Display.  You won't need any special software setup if you use the The Raspberry Pi Foundation 7" Touchscreen as it's compatible with Android Things. Other compatible touch screens may require additional configuration for Android Things. There are two options for installing the setting up your RPi 3 and installing the application. 

- (1) The first is to set up your RPi3 to use [Android Things 0.4.1-devpreview for Raspbery Pi 3](https://developer.android.com/things/hardware/raspberrypi.html). Clone the repository and compile the APK using Android Studio, then side load the APK file onto your device running Android Things Preview 0.4.1 using the ADB tool. 

- (2) The second option is to download the latest build from the [release](https://github.com/thanksmister/androidthings-mqtt-alarm-panel/releases/) section on Github which includes both the Android Things Preview 0.4.1 and the APK file for the MQTT control panel application. This also allows for future OTA updates.

 * Unzip the file to get the 'iot_rpi3.img'.
 * Burn image to an SD card using a tool like [Etcher](https://etcher.io/).
 * Insert the SD card into RPi3 and boot.  

- Be sure to set up network access either using WiFi or ethernet. If you setup WiFi be sure to unplug the Ethernet cable, at this time Android Things can't use both. 

```
# Use the adb tool to connect over ethernet to the device
adb connect Android.local

# Then set your your WiFi SSID and password
adb shell am startservice \
    -n com.google.wifisetup/.WifiSetupService \
    -a WifiSetupService.Connect \
    -e ssid <Network_SSID> \
    -e passphrase <Network_Passcode>
```

- You probably also want to set the time and [timezone](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones) of the device:

```
# Reboot ADB into root mode
$ adb root

# Set the date to 2017/12/31 12:00:00
$ adb shell date 123112002017.00

# Set the time zone to US Mountain Time
$ adb shell setprop persist.sys.timezone "America/Denver"
```
- Optionally connect the buzzer as shown in the [sampel diagram](https://github.com/androidthings/drivers-samples/tree/master/pwmspeaker).

# Alarm Setup

- Under the settings (gear icon) enter the MQTT information that you configured in Home Assistant for your MQTT service.
- Be sure you adjust the time intervals to match those set (other than defaults) in the Home Assistant MQTT alarm control panel.
- If you choose to get weather updates, enter your [DarkSky API key](https://darksky.net/dev) and your latitude and longitude location. You can get your lat/lon by using maps.google.com and copy them from the url (they look like -34.6156624,-58.5035102).
- To use a screen saver other than the digital clock, turn this feature on in the screen saver settings. Optionally you can load other Instagram images by changing the Instagram profile name.  

# Alarm Application Setup 
When you first start the application you will be asked to go to the setting screen and enter your pin code. You also need to enter the MQTT information that you configured in Home Assistant for your MQTT service. Be sure you adjust the time intervals to match those set in the Home Assistant MQTT alarm control panel. 

![alarm_settings](https://user-images.githubusercontent.com/142340/29889464-9f6874cc-8d9a-11e7-8e90-6f59787b5ef3.png)

The application should then connect to your MQTT broker. 
Whenever the state of the alarm control panel in  in Home Assistant chances, the alarm control panel will reflect those changes.  You need to enter you pin code whenever you want to disarm the alarm or if the alarm has been triggered.  

![alarm_disarm](https://user-images.githubusercontent.com/142340/29889459-9f557980-8d9a-11e7-996e-dcbfd54d44cc.png)

![alarm_triggered](https://user-images.githubusercontent.com/142340/29889462-9f6422dc-8d9a-11e7-923a-06cfcd6acff7.png)

To set the alarm just select the main icon on the alarm screen and then select Arm Home or Arm Away options.

![alarm_arm](https://user-images.githubusercontent.com/142340/29889458-9f33509e-8d9a-11e7-8bdf-aaad28d94328.png)

A small countdown will appear to indicate the time remaining before the alarm is activated. 

![alarm_pending](https://user-images.githubusercontent.com/142340/29889461-9f62d238-8d9a-11e7-9a0f-77baf385d812.png)

If you choose to get weather updates, enter your DarkSky API key and current latitude and longitude in the weather setting screen. You can get your lat/lon by using maps.google.com and copy them from the url (they look like -34.6156624,-58.5035102).

![alarm_weather](https://user-images.githubusercontent.com/142340/29889463-9f64e550-8d9a-11e7-8d06-cbb046588875.png)

To use a screen saver other than the digital clock, turn this feature on in the screen saver settings. Optionally you can load other Instagram images by changing the Instagram profile name.  

# Firebase + Crashlytics

![firebase_crashlytics](https://user-images.githubusercontent.com/142340/31292515-1a9ae1f8-aaaa-11e7-8384-e5ba3210f013.png)

The Alarm Control Panel, like many IoT devices will be used on hardware that may be disseminated throughout the world. Therefore we need a way to remotely track it's stability and alert us to any possible issues in the field. Fabric’s Crashlytics is now part of [Google's Cloud IoT Core](https://cloud.google.com/solutions/iot/) and [Firebase](http://firebase.google.com) services. [Crashlytics](https://fabric.io/kits/android/crashlytics/install) provides advanced tools for solving stability issues as well as engagement metrics using [Answers](https://www.fabric.io/kits/ios/answers). 

I have added this essential feature to the Alarm Control Panel application. The full RPi3 image from the from the release section already includes the Android Things preview 0.4.1 and the Alarm Control Panel application setup for Crashlytics reporting. You don't need to setup your own Crashlytics to use the application.  However, if you want to set up Crashlytics and Answers for your own application you can either install it following the [manual installation instructions](https://fabric.io/kits/android/crashlytics/install) or use the [Android Plugin](https://fabric.io/downloads/android-studio) for Android Studio or IntelliJ.  I recommend using the Android plugin as it's the simplest to setup and use.   

# Notes 

I have also made a version of the same software that runs on Android devices which can be found on [Google Play] (https://play.google.com/store/apps/details?id=com.thanksmister.iot.mqtt.alarmpanel).

At this time there is an issue dimming the brightness of the backlight for the display. So for now I have included a screen saver feature as a short-term fix until the bug is addressed by the Android Things development team.  I will update the application once I can use the screen dimming feature for Android Things.

There have been multiple display issues using Android Things 0.5.0 and 0.5.1, therefore this application runs best under Android Things 0.4.1. 

It's important that the alarm control panel settings reflect the settings of those used in the alarm control panel component. Initially the hardware control panel is set to the default settings of the alarm control panel component. There are settings options to match those used in the HASS manual alarm panel. 

# Acknowledgements

Special thanks to Colin O'Dell whose work on the Home Assistant Manual Alarm Control Panel component and his [MQTT Alarm Panel](https://github.com/colinodell/mqtt-control-panel) helped make this project possible.
