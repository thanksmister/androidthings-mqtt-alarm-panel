# Android Things Alarm Panel for Home Assistant

This project is a MQTT Alarm Control Panel for use with [Home Assistant's Manual Alarm Control Panel](https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) component. However, the application can be used with any home automation platform that supports the MQTT messaging protocol such as OpenHab, Node-RED, or cloud-based MQTT services. This project was built using a Raspberry Pi 3 and a 7" Touchscreen display running Android Things.

- [Alarm Panel Video](https://youtu.be/xspCZoRIBNQ)
- [Hackster.io Instructions](https://www.hackster.io/thanksmister/mqtt-alarm-control-panel-for-home-assistant-a206cc)
- [Android Device Version](https://github.com/thanksmister/android-mqtt-alarm-panel)


![alarm_panel](https://user-images.githubusercontent.com/142340/34173863-a2a8a4e4-e4d5-11e7-96b4-15a59a163243.jpg)

The alarm control panel acts as an interface for your own home alarm system and allows for two way communication using MQTT messaging.  You can set the alarm state to away or home, or disarm the alarm using a code.  Your home automation system will controls the sensors or automation that triggers the siren or notifies users of possible intrusion.  

To use the application with Home Assistant, you need to use the [Home Assistant's Manual Alarm Control Panel](https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) component and have the MQTT service setup and configured. In the alarm control panel application you enter your MQTT broker address, port number, and credentials if necessary.   If you are using an automation system other than Home Assistant, you will need to use the same publish states and commands in your setup.

## Support

For issues, feature requests, comments or questions, use the [Github issues tracker](https://github.com/thanksmister/androidthings-mqtt-alarm-panel/issues).  For HASS specific questions, you can join the [Home Assistant Community Dsicussion](https://community.home-assistant.io/t/mqtt-alarm-control-panel-for-raspberry-pi-and-android/26484/94) page which already has a lot information from the community. 

## Features
- Externally powered speaker for sound and notificaitons.
- Camera support for capturing and emailing images when alarm disabled (requires Mailgun api key).
- Google Text-to-Speech support to speak MQTT notification messages.
- Screen timout and brightness control with optional clock screensaver mode. 
- 7 day Weather forecast (requires Darksky api key).
- Home Automation Platform webpage support for viewing your home automation website.

## Supported Command and Publish States

- Command topic:  home/alarm/set, home/notification
- Command payloads: ARM_HOME, ARM_AWAY, DISARM
- Publish topic: home/alarm
- Publish payloads: disarmed, armed_away, armed_home, pending, triggered.

## Hardware

- Raspberry Pi 3 and SD Card.
- [7" Touchscreen Display for display](https://www.adafruit.com/product/2718).
- (Optional) Externally powered speaker for sound.
- (Optional) [Raspberry Pi Camera Module v2](https://www.raspberrypi.org/products/camera-module-v2/)

## Software to Build from Codebase (only if building from code)

- [Android Things 0.6.1-devpreview for Raspbery Pi 3](https://developer.android.com/things/hardware/raspberrypi.html) from the site or using the [Android Things Setup Utility](https://partner.android.com/things/console/u/0/#/tools).

- Android Studio with Android SDK N or above.

## Home Assistant Setup

- Setup [Home Assistant](https://home-assistant.io/getting-started/)
- Configure the [MQTT service](https://home-assistant.io/components/mqtt/) note the broker address, port and username/password if applicable.
- Add the [MQTT Alarm Control Panel] (https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) to your configuraiton with the default settings for now.
- Add any sensors (like Zwave door sensors or sirens) and configure automations to trigger the alarm.

## Raspberry PI Setup

Make sure you properly setup the RPi3 with the 7" Touchscreen Display.  You won't need any special software setup if you use the The Raspberry Pi Foundation 7" Touchscreen as it's compatible with Android Things. Other compatible touch screens may require additional configuration for Android Things. There are two options for installing the setting up your RPi 3 and installing the application. 

- (1) The first is to set up your RPi3 to use [Android Things 0.6.1-devpreview for Raspbery Pi 3](https://developer.android.com/things/hardware/raspberrypi.html). Clone the repository and compile the APK using Android Studio, then side load the APK file onto your device running Android Things Preview 0.6.1 using the ADB tool. 

- (2) The second option is to download the latest build from the [release](https://github.com/thanksmister/androidthings-mqtt-alarm-panel/releases/) section on Github which includes both the Android Things Preview 0.6.1 and the APK file for the MQTT control panel application. This also allows for future OTA updates.

 * Download the latest release zip: MQTT_Alarm_Control_Panel_1.x.x.zip.
 * Unzip the file to get the the image: iot_rpi3.img. 
 * Burn the image to your SD card using a tool like Etcher. 
 * Insert the SD card into RPi3 and boot.

- Be sure to set up network access either using WiFi or ethernet. If you setup WiFi be sure to unplug the Ethernet cable, at this time Android Things can't use WiFi and ethernet at the same time.  You can use the [Android Things Setup Utility](https://partner.android.com/things/console/u/0/#/tools) or use the adb command line tool.

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

- You probably also want to set the time and [timezone](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones) of the device. This can be done from within the application settings or manually using the adb command line tool:

```
# Reboot ADB into root mode
$ adb root

# Set the date to 2017/12/31 12:00:00
$ adb shell date 123112002017.00

# Set the time zone to US Mountain Time
$ adb shell setprop persist.sys.timezone "America/Denver"
```

## Device Settings

- Under the settings (gear icon) you can change the screen timeout time, screen brightness, and the screen density (for the official screen use a DPI of 160).
- Additionally you can set the time and timezone for the application. 

## Alarm Setup

- Under the settings (gear icon) enter the MQTT information that you configured in Home Assistant for your MQTT service.
- Be sure you adjust the time intervals to match those set (other than defaults) in the Home Assistant MQTT alarm control panel.
- If you choose to get weather updates, enter your [DarkSky API key](https://darksky.net/dev) and your latitude and longitude location. You can get your lat/lon by using maps.google.com and copy them from the url (they look like -34.6156624,-58.5035102).
- To use a screen saver other than the digital clock, turn this feature on in the screen saver settings. Optionally you can load other Instagram images by changing the Instagram profile name.  

## Alarm Application Setup 

When you first start the application you will be asked to go to the setting screen to setup the device. You also need to enter the MQTT information that you configured in Home Assistant for your MQTT service. Be sure you adjust the time intervals to match those set in the Home Assistant MQTT alarm control panel. Alsop reset your alarm pin code, the default is 1234. 

![alarm_settings](https://user-images.githubusercontent.com/142340/29889464-9f6874cc-8d9a-11e7-8e90-6f59787b5ef3.png)

The application should then connect to your MQTT broker. 
Whenever the state of the alarm control panel in  in Home Assistant chances, the alarm control panel will reflect those changes.  You need to enter you pin code whenever you want to disarm the alarm or if the alarm has been triggered.  

![alarm_home](https://user-images.githubusercontent.com/142340/29889460-9f615642-8d9a-11e7-99a6-1a49529dd580.png)

![alarm_disarm](https://user-images.githubusercontent.com/142340/29889459-9f557980-8d9a-11e7-996e-dcbfd54d44cc.png)

![alarm_triggered](https://user-images.githubusercontent.com/142340/29889462-9f6422dc-8d9a-11e7-923a-06cfcd6acff7.png)

To set the alarm just select the main icon on the alarm screen and then select Arm Home or Arm Away options.

![alarm_arm](https://user-images.githubusercontent.com/142340/29889458-9f33509e-8d9a-11e7-8bdf-aaad28d94328.png)

A small countdown will appear to indicate the time remaining before the alarm is activated. 

![alarm_pending](https://user-images.githubusercontent.com/142340/29889461-9f62d238-8d9a-11e7-9a0f-77baf385d812.png)

If you choose to get weather updates, enter your DarkSky API key and current latitude and longitude in the weather setting screen. You can get your lat/lon by using maps.google.com and copy them from the url (they look like -34.6156624,-58.5035102).

![alarm_weather](https://user-images.githubusercontent.com/142340/29889463-9f64e550-8d9a-11e7-8d06-cbb046588875.png)

You can also load your home automation platfgorm website by entering the address with port into the settings.  It slides from the right on the main screen. 

![platform_panel](https://user-images.githubusercontent.com/142340/34175188-53419a14-e4da-11e7-970a-77d2ff753d31.png)

To use a screen saver other than the black, turn this feature on in the screen saver settings. 

## Weather Updates (Darksky)

If you would like to get weather updates, create and enter a [Dark Sky API](https://darksky.net/dev/) key and your current latitude and longitude into the weather setting screen. You can get your current location by using maps.google.com in a web browser and copying the lat/lon from the url (they look like -34.6156624,-58.5035102 in the url).

To use a photo screensaver rather than the digital clock, turn this feature on, using the screen saver settings screen. You can load other Instagram images by changing the Instagram profile name in the settings.

## Capture Images (Telegram/Mailgun)

If you would like to capture and email images when the alarm is deactivated then you need to setup a [Mailgun](https://www.mailgun.com/) account. You will need to enter the domain address and API key from your Mailgun accoint into the application setting screen along with other information. 

You may also use Telegram to recieve a notification with the image when the alarm is deactivated.  To use Telegram you need a chat Id and a Telegram Bot API token.  Follow the [Telegram guide on Home Assistant](https://home-assistant.io/components/notify.telegram/) to setup Telegram.  Enter the chat Id and token into the application settings screen.

The camera only captures images when activated in the settings and MailGun is setup properly.  Images are captured each time the alarm is deactivated. You may use either Mailgun, Telegram, or both to send notifications. 

## Screensaver

To use a screen saver other than the digital clock, turn this feature on in the screen saver settings. You will need an Imgur key and a tag for which images you would like to use from [Imgur Client Id](https://apidocs.imgur.com/)

## Platform Screen

You can load your Home Assistant (or any web page) as alternative view by entering your Home Assistant address.  The address shuold be in the format http://192.168.86.240:8123 and include the port number.  You can use HADashboard or Home Assistant kiosk mode as well.  

This feature uses an Android web view component and may not work on older SDK versions.  There is also a limitation when saving username/passwords and screen reloading.  The username/passwords are not rememebered, and you will need to login again. I recommend using this application as on a dedicated tablet rather than your day-to-day device for best results.

## Enclosure

Originally I 3D printed an enclosure: https://www.thingiverse.com/thing:1082431. However, I ended up you want to buying a SmartPi Touch case which is a great option especially if you have the [Camera Module v2](https://www.raspberrypi.org/products/camera-module-v2/).

## Notes 

- I have also made a version of the same software that runs on Android devices which can be found on [Google Play] (https://play.google.com/store/apps/details?id=com.thanksmister.iot.mqtt.alarmpanel).

- It's important that the alarm control panel settings reflect the settings of those used in the alarm control panel component. Initially the hardware control panel is set to the default settings of the alarm control panel component. There are settings options to match those used in the HASS manual alarm panel. 

- To use TTS and the Camera you will need Android Lollipop SDK or greater as well as camera permissions. Older versions of Android are currently not supported.  The application is locked into the landscape mode for usability.  It is meant to run on dedicated tablets or large screen devices that will be used mainly for an alarm control panel. 

## Acknowledgements

Special thanks to Colin O'Dell who's work on the Home Assistant Manual Alarm Control Panel component and his [MQTT Alarm Panel](https://github.com/colinodell/mqtt-control-panel) helped make this project possible. Thanks to [Juan Manuel Vioque](https://github.com/tremebundo) for Spanish translations and [Gerben Bol](https://gerbenbol.com/) for Dutch translations.
