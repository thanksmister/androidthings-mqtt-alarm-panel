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

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
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

    private var inactivityHandler: Handler? = Handler()
    private var hasNetwork = AtomicBoolean(true)
    val disposable = CompositeDisposable()
    //var screenManager: ScreenManager? = null

    abstract fun getLayoutId(): Int

    private val inactivityCallback = Runnable {
        dialogUtils.hideScreenSaverDialog()
        showScreenSaver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MessageViewModel::class.java)

        // These are Android Things specific settings for setting the time, display, and update manager
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
    }

    override fun onStart(){
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (inactivityHandler != null) {
            inactivityHandler!!.removeCallbacks(inactivityCallback)
            inactivityHandler = null
        }
        disposable.dispose()
    }

    /**
     * Resets the screen timeout and brightness to the default (or user set) settings
     */
    fun setScreenDefaults() {
        ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(configuration.screenBrightness);
        ScreenManager(Display.DEFAULT_DISPLAY).setScreenOffTimeout(configuration.screenTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Keeps the screen on extra long time if the alarm is triggered.
     */
    fun setScreenTriggered() {
        ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(configuration.screenBrightness);
        ScreenManager(Display.DEFAULT_DISPLAY).setScreenOffTimeout(3, TimeUnit.HOURS);
    }

    fun setScreenBrightness(brightness: Int) {
        ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(brightness);
    }

    fun resetInactivityTimer() {
        Timber.d("resetInactivityTimer")
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler?.removeCallbacks(inactivityCallback)
        inactivityHandler?.postDelayed(inactivityCallback, configuration.inactivityTime)
    }

    fun stopDisconnectTimer() {
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler!!.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        Timber.d("onUserInteraction")
        resetInactivityTimer()
        setScreenDefaults()
    }

    public override fun onStop() {
        super.onStop()
        //stopDisconnectTimer()
    }

    public override fun onResume() {
        super.onResume()
        registerReceiver(connReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(connReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e.message)
        }
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
                    dialogUtils.hideScreenSaverDialog()
                    setScreenDefaults()
                    resetInactivityTimer()
                }
            }, View.OnClickListener {
                dialogUtils.hideScreenSaverDialog()
                setScreenDefaults()
                resetInactivityTimer()
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
    fun handleNetworkDisconnect() {
        dialogUtils.hideScreenSaverDialog()
        dialogUtils.showAlertDialogToDismiss(this@BaseActivity, getString(R.string.text_notification_network_title),
                    getString(R.string.text_notification_network_description))
    }

    /**
     * On network connect hide any alert dialogs generated by
     * the network disconnect and clear any notifications.
     */
    fun handleNetworkConnect() {
        dialogUtils.hideAlertDialog()
    }

    fun hasNetworkConnectivity(): Boolean {
        return hasNetwork.get()
    }

    /**
     * Network connectivity receiver to notify client of the network disconnect issues and
     * to clear any network notifications when reconnected. It is easy for network connectivity
     * to run amok that is why we only notify the user once for network disconnect with
     * a boolean flag.
     */
    private val connReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val currentNetworkInfo = connectivityManager.activeNetworkInfo
            if (currentNetworkInfo != null && currentNetworkInfo.isConnected) {
                Timber.d("Network Connected")
                hasNetwork.set(true)
                handleNetworkConnect()
            } else if (hasNetwork.get()) {
                Timber.d("Network Disconnected")
                hasNetwork.set(false)
                handleNetworkDisconnect()
            }
        }
    }
}