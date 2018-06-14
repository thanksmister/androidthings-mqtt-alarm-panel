/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui

import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_DISARM

import dpreference.DPreference
import javax.inject.Inject

/**
 * Store configurations
 */
class Configuration @Inject
constructor(private val sharedPreferences: DPreference) {

    var networkId: String?
        get() = this.sharedPreferences.getPrefString(PREF_NETWORK_ID, null)
        set(value) = this.sharedPreferences.setPrefString(PREF_NETWORK_ID, value)

    var networkPassword: String?
        get() = this.sharedPreferences.getPrefString(PREF_NETWORK_PASSWORD, null)
        set(value) = this.sharedPreferences.setPrefString(PREF_NETWORK_PASSWORD, value)

    var useNightDayMode: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_DAY_NIGHT_MODE, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_DAY_NIGHT_MODE, value)

    var dayNightMode: String
        get() = this.sharedPreferences.getPrefString(DISPLAY_MODE_DAY_NIGHT, DISPLAY_MODE_DAY)
        set(value) = this.sharedPreferences.setPrefString(DISPLAY_MODE_DAY_NIGHT, value)

    var nightModeChanged: Boolean
        get() = this.sharedPreferences.getPrefBoolean(DISPLAY_MODE_DAY_NIGHT_CHANGED, false)
        set(value) = this.sharedPreferences.setPrefBoolean(DISPLAY_MODE_DAY_NIGHT_CHANGED, value)

    var isPortraitMode: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_DEVICE_SCREEN_POTRAIT, false)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_DEVICE_SCREEN_POTRAIT, value)

    var dayNightModeStartTime: String
        get() = this.sharedPreferences.getPrefString(PREF_MODE_DAY_NIGHT_START, DAY_NIGHT_START_VALUE_DEFAULT)
        set(value) = this.sharedPreferences.setPrefString(PREF_MODE_DAY_NIGHT_START, value)

    var dayNightModeEndTime: String
        get() = this.sharedPreferences.getPrefString(PREF_MODE_DAY_NIGHT_END, DAY_NIGHT_END_VALUE_DEFAULT)
        set(value) = this.sharedPreferences.setPrefString(PREF_MODE_DAY_NIGHT_END, value)

    var webUrl: String?
        get() = this.sharedPreferences.getPrefString(PREF_WEB_URL, null)
        set(value) = this.sharedPreferences.setPrefString(PREF_WEB_URL, value)

    var hideAdminMenu: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_PLATFORM_ADMIN_MENU, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_PLATFORM_ADMIN_MENU, value)

    var adjustBackBehavior: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_PLATFORM_BACK_BEHAVIOR, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_PLATFORM_BACK_BEHAVIOR, value)

    var alarmMode: String
        get() = this.sharedPreferences.getPrefString(PREF_ALARM_MODE, MODE_DISARM)
        set(value) = this.sharedPreferences.setPrefString(PREF_ALARM_MODE, value)

    var inactivityTime: Long
        get() = this.sharedPreferences.getPrefLong(PREF_INACTIVITY_TIME, 300000)
        set(value) = this.sharedPreferences.setPrefLong(PREF_INACTIVITY_TIME, value)

    var timeZone: String
        get() = this.sharedPreferences.getPrefString(PREF_DEVICE_TIME_ZONE, "America/Los_Angeles")
        set(value) = this.sharedPreferences.setPrefString(PREF_DEVICE_TIME_ZONE, value)

    var screenBrightness: Int
        get() = this.sharedPreferences.getPrefInt(PREF_DEVICE_SCREEN_BRIGHTNESS, 5)
        set(value) = this.sharedPreferences.setPrefInt(PREF_DEVICE_SCREEN_BRIGHTNESS, value)

    var isFirstTime: Boolean
        get() = sharedPreferences.getPrefBoolean(PREF_FIRST_TIME, true)
        set(value) = sharedPreferences.setPrefBoolean(PREF_FIRST_TIME, value)

    var pendingTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_PENDING_TIME, AlarmUtils.PENDING_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_PENDING_TIME, value)

    var delayTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_DELAY_TIME, AlarmUtils.DELAY_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_DELAY_TIME, value)

    var delayHomeTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_HOME_DELAY_TIME, AlarmUtils.DELAY_HOME_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_HOME_DELAY_TIME, value)

    var delayAwayTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_AWAY_DELAY_TIME, AlarmUtils.DELAY_AWAY_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_AWAY_DELAY_TIME, value)

    var pendingHomeTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_HOME_PENDING_TIME, AlarmUtils.PENDING_HOME_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_HOME_PENDING_TIME, value)

    var pendingAwayTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_AWAY_PENDING_TIME, AlarmUtils.PENDING_AWAY_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_AWAY_PENDING_TIME, value)

    var disableTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_DISABLE_DIALOG_TIME, AlarmUtils.DISABLE_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_DISABLE_DIALOG_TIME, value)

    var alarmCode: Int
        get() = this.sharedPreferences.getPrefInt(PREF_ALARM_CODE, 1234)
        set(value) = this.sharedPreferences.setPrefInt(PREF_ALARM_CODE, value)

    var timeFormat: Int
        get() = sharedPreferences.getPrefInt(PREF_DEVICE_TIME_FORMAT, 12)
        set(value) = sharedPreferences.setPrefInt(PREF_DEVICE_TIME_FORMAT, value)

    var useTimeServer: Boolean
        get() = sharedPreferences.getPrefBoolean(PREF_DEVICE_TIME_SERVER, true)
        set(value) = sharedPreferences.setPrefBoolean(PREF_DEVICE_TIME_SERVER, value)

    var platformBar: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_PLATFORM_BAR, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_PLATFORM_BAR, value)

    var telegramChatId: String
        get() = this.sharedPreferences.getPrefString(PREF_TELEGRAM_CHAT_ID, "")
        set(value) = this.sharedPreferences.setPrefString(PREF_TELEGRAM_CHAT_ID, value)

    var telegramToken: String
        get() = this.sharedPreferences.getPrefString(PREF_TELEGRAM_TOKEN, "")
        set(value) = this.sharedPreferences.setPrefString(PREF_TELEGRAM_TOKEN, value)

    var mqttImage: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_MQTT_IMAGE, false)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_MQTT_IMAGE, value)

    fun hasTelegramCredentials(): Boolean {
        return !TextUtils.isEmpty(telegramChatId) && !TextUtils.isEmpty(telegramToken)
    }

    fun hasPlatformModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_WEB, false)
    }

    fun setWebModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_WEB, value)
    }

    fun hasTssModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_TSS, false)
    }

    fun setTssModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_TSS, value)
    }

    fun hasAlertsModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_ALERTS, false)
    }

    fun setAlertsModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_ALERTS, value)
    }

    fun showClockScreenSaverModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_CLOCK_SAVER, false)
    }

    fun setClockScreenSaverModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_CLOCK_SAVER, value)
    }

    fun showPhotoScreenSaver(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_PHOTO_SAVER, false)
    }

    fun setPhotoScreenSaver(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_PHOTO_SAVER, value)
    }

    fun setShowWeatherModule(show: Boolean) {
        sharedPreferences.setPrefBoolean(PREF_MODULE_WEATHER, show)
    }

    fun showWeatherModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_WEATHER, false)
    }

    fun hasNotifications(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_NOTIFICATION, false)
    }

    fun setHasNotifications(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_NOTIFICATION, value)
    }

    fun getMailTo(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_TO, null)
    }

    fun setMailTo(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_TO, value)
    }

    fun getMailFrom(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_FROM, null)
    }

    fun setMailFrom(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_FROM, value)
    }

    fun getMailGunApiKey(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_API_KEY, null)
    }

    fun setMailGunApiKey(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_API_KEY, value)
    }

    fun getMailGunUrl(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_URL, null)
    }

    fun setMailGunUrl(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_URL, value)
    }

    fun hasMailGunCredentials(): Boolean {
        return !TextUtils.isEmpty(getMailGunUrl()) && !TextUtils.isEmpty(getMailGunApiKey())
                && !TextUtils.isEmpty(getMailTo()) && !TextUtils.isEmpty(getMailFrom())
    }

    fun hasCamera(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_CAMERA, false)
    }

    fun setHasCamera(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_CAMERA, value)
    }

    fun getCameraRotate(): Float? {
        return sharedPreferences.getPrefString(PREF_CAMERA_ROTATE, "0f").toFloat()
    }

    fun setCameraRotate(value: String) {
        sharedPreferences.setPrefString(PREF_CAMERA_ROTATE, value)
    }

    fun isAlarmTriggeredMode(): Boolean {
        return alarmMode == AlarmUtils.MODE_TRIGGERED
                || alarmMode == AlarmUtils.MODE_HOME_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_TRIGGERED_PENDING
    }

    fun isAlarmPendingMode(): Boolean {
        return (alarmMode == AlarmUtils.MODE_ARM_AWAY_PENDING
                || alarmMode == AlarmUtils.MODE_ARM_HOME_PENDING
                || alarmMode == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_HOME_TRIGGERED_PENDING)
    }

    fun isAlarmDisableMode(): Boolean {
        return (alarmMode == AlarmUtils.MODE_ARM_HOME
                || alarmMode == AlarmUtils.MODE_ARM_AWAY
                || alarmMode == AlarmUtils.MODE_HOME_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_TRIGGERED_PENDING)
    }

    fun hasScreenSaver() : Boolean {
        return (showPhotoScreenSaver() || showClockScreenSaverModule())
    }

    /**
     * Reset the `SharedPreferences` and database
     */
    fun reset() {
        sharedPreferences.removePreference(PREF_PENDING_TIME)
        sharedPreferences.removePreference(PREF_MODULE_CLOCK_SAVER)
        sharedPreferences.removePreference(PREF_MODULE_PHOTO_SAVER)
        sharedPreferences.removePreference(PREF_INACTIVITY_TIME)
        sharedPreferences.removePreference(PREF_MODULE_WEATHER)
        sharedPreferences.removePreference(PREF_MODULE_WEB)
        sharedPreferences.removePreference(PREF_MODULE_NOTIFICATION)
        sharedPreferences.removePreference(PREF_WEB_URL)
        sharedPreferences.removePreference(PREF_FIRST_TIME)
        sharedPreferences.removePreference(PREF_MAIL_TO)
        sharedPreferences.removePreference(PREF_MAIL_FROM)
        sharedPreferences.removePreference(PREF_MODULE_CAMERA)
        sharedPreferences.removePreference(PREF_MAIL_API_KEY)
        sharedPreferences.removePreference(PREF_MAIL_URL)
        sharedPreferences.removePreference(PREF_CAMERA_ROTATE)
        sharedPreferences.removePreference(PREF_MODULE_TSS)
        sharedPreferences.removePreference(PREF_MODULE_ALERTS)
        sharedPreferences.removePreference(PREF_DEVICE_TIME_ZONE)
        sharedPreferences.removePreference(PREF_DEVICE_TIME)
        sharedPreferences.removePreference(PREF_DEVICE_TIME_FORMAT)
        sharedPreferences.removePreference(PREF_DEVICE_TIME_SERVER)
        sharedPreferences.removePreference(PREF_DEVICE_SCREEN_BRIGHTNESS)
        sharedPreferences.removePreference(PREF_AWAY_DELAY_TIME)
        sharedPreferences.removePreference(PREF_HOME_DELAY_TIME)
        sharedPreferences.removePreference(PREF_DELAY_TIME)
        sharedPreferences.removePreference(PREF_AWAY_PENDING_TIME)
        sharedPreferences.removePreference(PREF_HOME_PENDING_TIME)
        sharedPreferences.removePreference(PREF_PLATFORM_BAR)
        sharedPreferences.removePreference(PREF_TELEGRAM_MODULE)
        sharedPreferences.removePreference(PREF_TELEGRAM_CHAT_ID)
        sharedPreferences.removePreference(PREF_TELEGRAM_TOKEN)
        sharedPreferences.removePreference(PREF_PLATFORM_BACK_BEHAVIOR)
        sharedPreferences.removePreference(PREF_PLATFORM_ADMIN_MENU)
        sharedPreferences.removePreference(PREF_MQTT_IMAGE)
        sharedPreferences.removePreference(PREF_NETWORK_ID)
        sharedPreferences.removePreference(PREF_NETWORK_PASSWORD)
    }

    companion object {
        const val PREF_PLATFORM_BACK_BEHAVIOR = "pref_platform_back_behavior"
        const val PREF_PLATFORM_ADMIN_MENU = "pref_platform_admin_menu"
        const val PREF_PENDING_TIME = "pref_pending_time"
        const val PREF_HOME_PENDING_TIME = "pref_home_pending_time"
        const val PREF_AWAY_PENDING_TIME = "pref_away_pending_time"
        const val PREF_DELAY_TIME = "pref_delay_time"
        const val PREF_HOME_DELAY_TIME = "pref_home_delay_time"
        const val PREF_AWAY_DELAY_TIME = "pref_away_delay_time"
        const val PREF_ALARM_CODE = "pref_alarm_code"
        const val PREF_ALARM_MODE = "pref_alarm_mode"
        const val PREF_MODULE_CLOCK_SAVER = "pref_module_saver_clock"
        const val PREF_MODULE_PHOTO_SAVER = "pref_module_saver_photo"
        const val PREF_IMAGE_SOURCE = "pref_image_source"
        const val PREF_IMAGE_FIT_SIZE = "pref_image_fit"
        const val PREF_IMAGE_ROTATION = "pref_image_rotation"
        const val PREF_INACTIVITY_TIME = "pref_inactivity_time"
        const val PREF_MODULE_NOTIFICATION = "pref_module_notification"
        const val PREF_MODULE_TSS = "pref_module_tss"
        const val PREF_MODULE_ALERTS = "pref_module_alerts"
        const val PREF_MAIL_TO = "pref_mail_to"
        const val PREF_MAIL_FROM = "pref_mail_from"
        const val PREF_MAIL_API_KEY = "pref_mail_api_key"
        const val PREF_MAIL_URL = "pref_mail_url"
        const val PREF_DISABLE_DIALOG_TIME = "pref_disable_dialog_time" // this isn't configurable
        const val PREF_IMAGE_CLIENT_ID = "pref_image_client_id"
        const val PREF_MODULE_CAMERA = "pref_module_camera"
        const val PREF_CAMERA_ROTATE = "pref_camera_rotate"
        const val PREF_MODULE_WEATHER = "pref_module_weather"
        const val PREF_MODULE_WEB = "pref_module_web"
        const val PREF_WEB_URL = "pref_web_url"
        const val PREF_FIRST_TIME = "pref_first_time"
        const val PREF_DEVICE_TIME_SERVER = "pref_device_time_server"
        const val PREF_DEVICE_TIME_FORMAT = "pref_device_time_format"
        const val PREF_DEVICE_TIME = "pref_device_time"
        const val PREF_DEVICE_TIME_ZONE = "pref_device_time_zone"
        const val PREF_DEVICE_SCREEN_BRIGHTNESS = "pref_screen_brightness"
        const val PREF_PLATFORM_BAR = "pref_platform_bar"
        const val PREF_TELEGRAM_MODULE = "pref_telegram_module"
        const val PREF_TELEGRAM_CHAT_ID = "pref_telegram_chat_id"
        const val PREF_TELEGRAM_TOKEN = "pref_telegram_token"
        const val PREF_MQTT_IMAGE = "pref_mqtt_image"
        const val PREF_MQTT_IMAGE_TOPIC = "pref_mqtt_image_topic"
        const val PREF_NETWORK_ID = "pref_network_id"
        const val PREF_NETWORK_PASSWORD = "pref_network_pass"
        const val PREF_DEVICE_SCREEN_POTRAIT = "pref_screen_portrait"
        const val PREF_REQUIRE_SCREEN_ROTATE = "pref_screen_rotate_required"
        const val PREF_DAY_NIGHT_MODE = "pref_day_night_mode"
        const val PREF_MODE_DAY_NIGHT_END = "mode_day_night_end"
        const val PREF_MODE_DAY_NIGHT_START = "mode_day_night_start"
        private const val DISPLAY_MODE_DAY_NIGHT = "mode_day_night"
        private const val DISPLAY_MODE_DAY_NIGHT_CHANGED = "mode_day_night_changed"
        const val DISPLAY_MODE_DAY = "mode_day"
        const val DISPLAY_MODE_NIGHT = "mode_night"
        const val DAY_NIGHT_START_VALUE_DEFAULT = "19:00"
        const val DAY_NIGHT_END_VALUE_DEFAULT = "6:00"
    }
}