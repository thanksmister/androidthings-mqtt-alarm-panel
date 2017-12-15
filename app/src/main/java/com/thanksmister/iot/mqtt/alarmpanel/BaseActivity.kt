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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.InstagramOptions
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
    public val disposable = CompositeDisposable()

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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (inactivityHandler != null) {
            inactivityHandler!!.removeCallbacks(inactivityCallback)
            inactivityHandler = null
        }
        disposable.dispose()
    }

    fun resetInactivityTimer() {
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler!!.removeCallbacks(inactivityCallback)
        inactivityHandler!!.postDelayed(inactivityCallback, configuration.inactivityTime)
    }

    fun stopDisconnectTimer() {
        dialogUtils.hideScreenSaverDialog()
        inactivityHandler!!.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        resetInactivityTimer()
    }

    public override fun onStop() {
        super.onStop()
        stopDisconnectTimer()
    }

    public override fun onResume() {
        super.onResume()
        if(connReceiver != null) {
            registerReceiver(connReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
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

    fun readImageOptions(): InstagramOptions {
        return InstagramOptions.from(preferences)
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
        if (!viewModel.isAlarmTriggeredMode()) {
            inactivityHandler!!.removeCallbacks(inactivityCallback)
            dialogUtils.showScreenSaver(this@BaseActivity, configuration.showPhotoScreenSaver(),
                    readImageOptions().getImageSource()!!, readImageOptions().imageFitScreen,
                    readImageOptions().imageRotation, object : ScreenSaverView.ViewListener {
                override fun onMotion() {
                    dialogUtils.hideScreenSaverDialog()
                    resetInactivityTimer()
                }
            }, View.OnClickListener {
                dialogUtils.hideScreenSaverDialog()
                resetInactivityTimer()
            })
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
        dialogUtils.showAlertDialogToDismiss(getString(R.string.text_notification_network_title),
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