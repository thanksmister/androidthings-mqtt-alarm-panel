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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.support.v7.app.AlertDialog
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.things.device.ScreenManager
import com.google.android.things.device.TimeManager
import com.google.android.things.update.UpdateManager
import com.google.android.things.update.UpdateManager.POLICY_APPLY_AND_REBOOT
import com.google.android.things.update.UpdateManager.POLICY_CHECKS_ONLY
import com.google.android.things.update.UpdateManagerStatus
import com.google.android.things.update.UpdatePolicy
import com.thanksmister.iot.mqtt.alarmpanel.managers.ConnectionLiveData
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MessageViewModel
import dagger.android.support.DaggerAppCompatActivity
import dpreference.DPreference
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var preferences: DPreference
    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MessageViewModel
    private var wakeLock: PowerManager.WakeLock? = null
    private var inactivityHandler: Handler? = Handler()
    private var hasNetwork = AtomicBoolean(true)
    val disposable = CompositeDisposable()
    private var connectionLiveData: ConnectionLiveData? = null

    abstract fun getLayoutId(): Int

    private val inactivityCallback = Runnable {
        dialogUtils.hideScreenSaverDialog()
        showScreenSaver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MessageViewModel::class.java)
    }

    override fun onStart(){
        super.onStart()
        // These are Android Things specific settings for setting the time, display, and update manager
        val handler = Handler()
        handler.postDelayed({ setSystemInformation() }, 1000)

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData?.observe(this, Observer { connected ->
            if(connected!!) {
                handleNetworkConnect()
            } else {
                handleNetworkDisconnect()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (inactivityHandler != null) {
            inactivityHandler!!.removeCallbacks(inactivityCallback)
            inactivityHandler = null
        }
        disposable.dispose()
        releaseTemporaryWakeLock()
    }

    // These are Android Things specific settings for setting the time, display, and update manager
    private fun setSystemInformation() {
        try {

            ScreenManager(Display.DEFAULT_DISPLAY).setBrightnessMode(ScreenManager.BRIGHTNESS_MODE_MANUAL);
            ScreenManager(Display.DEFAULT_DISPLAY).setScreenOffTimeout(configuration.screenTimeout, TimeUnit.MILLISECONDS);
            ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(configuration.screenBrightness);
            ScreenManager(Display.DEFAULT_DISPLAY).setDisplayDensity(configuration.screenDensity);

            TimeManager().setTimeZone(configuration.timeZone)

            UpdateManager()
                    .setPolicy(UpdatePolicy.Builder()
                            .setPolicy(POLICY_CHECKS_ONLY)
                            .setApplyDeadline(2, TimeUnit.DAYS)
                            .build())

            UpdateManager()
                    .addStatusListener { updateManagerStatus ->
                        if (updateManagerStatus.currentState == UpdateManagerStatus.STATE_UPDATE_AVAILABLE) {
                            AlertDialog.Builder(this@BaseActivity, R.style.CustomAlertDialog)
                                    .setMessage(getString(R.string.text_update_available))
                                    .setPositiveButton(android.R.string.ok, { _, _ ->
                                        UpdateManager().performUpdateNow(POLICY_APPLY_AND_REBOOT)
                                    })
                                    .show()
                        }
                    }
        } catch (e:IllegalStateException) {
            Timber.e(e.message)
        }
    }

    /**
     * Resets the screen timeout and brightness to the default (or user set) settings
     */
    fun setScreenDefaults() {
        Timber.d("setScreenDefaults")
        Timber.d("screenBirghness: " + configuration.screenBrightness)
        Timber.d("setScreenOffTimeout: " + configuration.screenTimeout)
        ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(configuration.screenBrightness);
        ScreenManager(Display.DEFAULT_DISPLAY).setScreenOffTimeout(configuration.screenTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Keeps the screen on extra long time if the alarm is triggered.
     */
    fun setScreenTriggered() {
        Timber.d("setScreenTriggered")
        ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(configuration.screenBrightness);
        ScreenManager(Display.DEFAULT_DISPLAY).setScreenOffTimeout(3, TimeUnit.HOURS); // 3 hours
    }

    private fun setScreenBrightness(brightness: Int) {
        ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(brightness);
        ScreenManager(Display.DEFAULT_DISPLAY).setScreenOffTimeout(configuration.screenTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Wakes the device temporarily (or always if triggered) when the alarm requires attention.
     * We should hold the wakelock the same amount of time as the screen off timeout.
     */
    fun acquireTemporaryWakeLock() {
        Timber.d("acquireTemporaryWakeLock")
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALARM_WAKE_TAG")
        }
        if (wakeLock != null && !wakeLock!!.isHeld()) {  // but we don't hold it
            if (viewModel.isAlarmTriggeredMode()) {
                wakeLock!!.acquire(10800000) // 3 hours in milliseconds
            } else {
                wakeLock!!.acquire(configuration.screenTimeout)
            }
        }
    }

    /**
     * Wakelock used to temporarily bring application to foreground if alarm needs attention.
     */
    fun releaseTemporaryWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld()) {
            wakeLock!!.release()
        }
    }

    fun resetInactivityTimer() {
        Timber.d("resetInactivityTimer")
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler?.removeCallbacks(inactivityCallback)
        inactivityHandler?.postDelayed(inactivityCallback, configuration.inactivityTime)
    }

    fun stopDisconnectTimer() {
        Timber.d("stopDisconnectTimer")
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler!!.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        Timber.d("onUserInteraction")
        resetInactivityTimer()
        setScreenDefaults()
        releaseTemporaryWakeLock()
    }

    public override fun onStop() {
        super.onStop()
    }

    public override fun onResume() {
        super.onResume()
        acquireTemporaryWakeLock()
    }

    override fun onPause() {
        super.onPause()
    }

    fun readMqttOptions(): MQTTOptions {
        return MQTTOptions.from(preferences)
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
        //dialogUtils.clearDialogs()
        if (!viewModel.isAlarmTriggeredMode() && viewModel.hasScreenSaver()) {
            inactivityHandler!!.removeCallbacks(inactivityCallback)
            if(configuration.showClockScreenSaverModule()) {
                setScreenBrightness(40);
            } else if (configuration.showPhotoScreenSaver()) {
                setScreenBrightness(80);
            }
            dialogUtils.showScreenSaver(this@BaseActivity,
                    configuration.showPhotoScreenSaver(), configuration.showClockScreenSaverModule(),
                    readImageOptions(), object : ScreenSaverView.ViewListener {
                override fun onMotion() {
                    resetInactivityTimer()
                    setScreenDefaults()
                }
            }, View.OnClickListener {
                resetInactivityTimer()
                setScreenDefaults()
            })
        } else if (!viewModel.isAlarmTriggeredMode()) {
            setScreenBrightness(0);
        }
    }

    /**
     * On network disconnect show notification or alert message, clear the
     * screen saver and awake the screen. Override this method in activity
     * to for extra network disconnect handling such as bring application
     * into foreground.
     */
    open fun handleNetworkDisconnect() {
        acquireTemporaryWakeLock()
        dialogUtils.hideScreenSaverDialog()
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
}