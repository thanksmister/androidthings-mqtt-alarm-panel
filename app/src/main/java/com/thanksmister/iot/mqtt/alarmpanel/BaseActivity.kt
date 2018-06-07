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

package com.thanksmister.iot.mqtt.alarmpanel

import android.app.AlarmManager
import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.things.device.TimeManager
import com.google.android.things.update.StatusListener
import com.google.android.things.update.UpdateManager
import com.google.android.things.update.UpdateManagerStatus
import com.google.android.things.update.UpdatePolicy.POLICY_APPLY_AND_REBOOT
import com.thanksmister.iot.mqtt.alarmpanel.managers.ConnectionLiveData
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSkyDao
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DeviceUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.NetworkUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MessageViewModel
import dagger.android.support.DaggerAppCompatActivity
import dpreference.DPreference
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var preferences: DPreference
    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var darkSkyDataSource: DarkSkyDao
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MessageViewModel
    private var inactivityHandler: Handler = Handler()
    private var hasNetwork = AtomicBoolean(true)
    val disposable = CompositeDisposable()
    private var connectionLiveData: ConnectionLiveData? = null
    private var wifiManager: WifiManager? = null

    abstract fun getLayoutId(): Int

    private val inactivityCallback = Runnable {
        showScreenSaver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(getLayoutId())

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MessageViewModel::class.java)

        if( !TextUtils.isEmpty(configuration.networkId) && !TextUtils.isEmpty(configuration.networkPassword)) {
            NetworkUtils.connectNetwork(this@BaseActivity, configuration.networkId, configuration.networkPassword )
        }

        // TODO implement day/night using new Manager architecture compononent
        //setScreenDimTimeChecks()
        //setNightDayMode()
    }

    override fun onStart(){
        super.onStart()
        // These are Android Things specific settings for setting the time, display, and update manager
        val handler = Handler()
        handler.postDelayed({ setSystemInformation() }, 1000)
        application.registerReceiver(wifiConnectionReceiver, intentFilterForWifiConnectionReceiver)
    }

    public override fun onStop() {
        super.onStop()
        application.unregisterReceiver(wifiConnectionReceiver)
    }

    public override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        Timber.d("requiresScreenRotate: ${configuration.requiresScreenRotate()}")
        Timber.d("isPortraitMode: ${configuration.isPortraitMode()}")
        if(configuration.requiresScreenRotate()) {
            configuration.setRequiresScreenRotate(false)
            if(configuration.isPortraitMode()) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityHandler.removeCallbacks(inactivityCallback)
        disposable.dispose()
    }

    // These are Android Things specific settings for setting the time, display, and update manager
    private fun setSystemInformation() {
        try {
            TimeManager.getInstance().setTimeZone(configuration.timeZone)
            UpdateManager.getInstance().addStatusListener(object: StatusListener {
                override fun onStatusUpdate(status: UpdateManagerStatus?) {
                    when (status?.currentState) {
                        UpdateManagerStatus.STATE_UPDATE_AVAILABLE -> {
                            Timber.d("Update available")
                            Toast.makeText(this@BaseActivity, getString(R.string.progress_updating), Toast.LENGTH_LONG).show()
                        }
                        UpdateManagerStatus.STATE_DOWNLOADING_UPDATE -> {
                            Timber.d("Update downloading")
                            Toast.makeText(this@BaseActivity, getString(R.string.progress_updating), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            });
            UpdateManager.getInstance().performUpdateNow(POLICY_APPLY_AND_REBOOT) // always apply update and reboot
        } catch (e:IllegalStateException) {
            Timber.e(e.message)
        }

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData?.observe(this, Observer { connected ->
            if(connected!!) {
                handleNetworkConnect()
            } else {
                handleNetworkDisconnect()
            }
        })
    }

    /**
     * Resets the screen brightness to the default or based on time of day
     */
    fun setScreenBrightness() {
        val brightness = DeviceUtils.getScreenBrightnessBasedOnDayTime(configuration.screenBrightness)
        Timber.d("setScreenBrightness: " + brightness)
        val lp: WindowManager.LayoutParams = window.attributes;
        lp.screenBrightness = brightness;
        window.attributes = lp
    }

    fun resetInactivityTimer() {
        Timber.d("resetInactivityTimer")
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, configuration.inactivityTime)
        setScreenBrightness()
    }

    fun stopDisconnectTimer() {
        Timber.d("stopDisconnectTimer")
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler.removeCallbacks(inactivityCallback)
        setScreenBrightness()
    }

    override fun onUserInteraction() {
        Timber.d("onUserInteraction")
        resetInactivityTimer()
        setScreenBrightness()
    }

    fun readWeatherOptions(): DarkSkyOptions {
        return DarkSkyOptions.from(preferences)
    }

    fun readImageOptions(): ImageOptions {
        return ImageOptions.from(preferences)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.itemId == android.R.id.home
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this.
     */
    open fun showScreenSaver() {
        Timber.d("showScreenSaver")
        if (!viewModel.isAlarmTriggeredMode() && viewModel.hasScreenSaver()) {
            inactivityHandler.removeCallbacks(inactivityCallback)
            val hasWeather = (configuration.showWeatherModule() && readWeatherOptions().isValid)
            dialogUtils.showScreenSaver(this@BaseActivity,
                    configuration.showPhotoScreenSaver(),
                    readImageOptions(),
                    configuration.screenBrightness,
                    View.OnClickListener {
                        dialogUtils.hideScreenSaverDialog()
                        setScreenBrightness()
                        resetInactivityTimer()
                    }, darkSkyDataSource, hasWeather)
        }
    }

    /**
     * On network disconnect show notification or alert message, clear the
     * screen saver and awake the screen. Override this method in activity
     * to for extra network disconnect handling such as bring application
     * into foreground.
     */
    open fun handleNetworkDisconnect() {
        dialogUtils.hideScreenSaverDialog()
        setScreenBrightness()
        dialogUtils.showAlertDialogToDismiss(this@BaseActivity, getString(R.string.text_notification_network_title),
                    getString(R.string.text_notification_network_description))
        hasNetwork.set(false)
    }

    /**
     * On network connect hide any alert dialogs generated by
     * the network disconnect and clear any notifications.
     */
    open fun handleNetworkConnect() {
        dialogUtils.hideAlertDialog()
        hasNetwork.set(true)
    }

    open fun hasNetworkConnectivity(): Boolean {
        return hasNetwork.get()
    }

    private val intentFilterForWifiConnectionReceiver: IntentFilter
        get() {
            val randomIntentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
            randomIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            randomIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
            return randomIntentFilter
        }

    private val wifiConnectionReceiver: BroadcastReceiver = object: BroadcastReceiver()  {
        override fun onReceive(c: Context, intent: Intent) {
            val action = intent.action
            if (!TextUtils.isEmpty(action)) {
                when (action) {
                    WifiManager.WIFI_STATE_CHANGED_ACTION,
                    WifiManager.SUPPLICANT_STATE_CHANGED_ACTION -> {
                        val wifiInfo = wifiManager?.connectionInfo
                        var wirelessNetworkName = wifiInfo?.ssid
                        wirelessNetworkName = wirelessNetworkName?.replace("\"", "");
                        Timber.d("networkId: $wirelessNetworkName")
                    }
                }
            }
        }
    }

    private fun setScreenNightTime() {

    }

    private fun setNightDayMode() {
        Timber.d("setNightDayMode")
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hourOfDay >= 21 || hourOfDay < 6) {
            Timber.d("Tis the night")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate()
        } else {
            Timber.d("Tis the day")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
            recreate()
        }
        setScreenBrightness()
    }

    private fun setScreenDimTimeChecks() {
        val alarmIntent = Intent(this@BaseActivity, ScreenDimCheckAlarmReceiver::class.java);
        val pendingIntent = PendingIntent.getBroadcast(this@BaseActivity, 0, alarmIntent, 0);
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startCalendar = Calendar.getInstance()
        startCalendar.set(Calendar.HOUR_OF_DAY, 15)
        startCalendar.set(Calendar.MINUTE, 20)
        startCalendar.set(Calendar.SECOND, 0)

        var startTime = startCalendar.timeInMillis
        if (System.currentTimeMillis() > startTime) {
            startTime += AlarmManager.INTERVAL_DAY
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                startTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent)

        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.HOUR_OF_DAY, 15)
        endCalendar.set(Calendar.MINUTE, 25)
        endCalendar.set(Calendar.SECOND, 0)

        var endTime = endCalendar.timeInMillis
        if (System.currentTimeMillis() > endTime) {
            endTime += AlarmManager.INTERVAL_DAY
        }
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                endTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent)
    }

    inner class ScreenDimCheckAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("ScreenDimCheckAlarmReceiver")
            setNightDayMode()
        }
    }
}