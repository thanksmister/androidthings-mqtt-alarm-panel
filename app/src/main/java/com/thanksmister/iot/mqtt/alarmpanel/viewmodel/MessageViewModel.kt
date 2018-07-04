/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.graphics.Bitmap
import android.text.TextUtils
import android.widget.Toast
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkStatus
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseApplication
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Message
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageDao
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.MailGunModule
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.TelegramModule
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_AWAY_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_HOME_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_AWAY_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_HOME_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_TRIGGERED
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.IMAGE_CAPTURE_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.NOTIFICATION_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessageViewModel @Inject
constructor(application: Application, private val messageDataSource: MessageDao,
            private val dataSource: MessageDao, private val configuration: Configuration) : AndroidViewModel(application) {

    private var armed: Boolean = false
    private val disposable = CompositeDisposable()

    @AlarmUtils.AlarmStates
    private fun setAlarmModeFromState(state: String) {
        if(state == AlarmUtils.STATE_PENDING) {
            if (getAlarmMode().equals(MODE_ARM_HOME) || getAlarmMode().equals(MODE_ARM_AWAY)) {
                if (getAlarmMode().equals(MODE_ARM_HOME)){
                    setAlarmMode(MODE_HOME_TRIGGERED_PENDING);
                } else if(getAlarmMode().equals(MODE_ARM_AWAY)) {
                    setAlarmMode(MODE_AWAY_TRIGGERED_PENDING);
                } else {
                    setAlarmMode(MODE_TRIGGERED_PENDING);
                }
            }
        } else if (state == AlarmUtils.STATE_TRIGGERED) {
            setAlarmMode(MODE_TRIGGERED)
        }
    }

    fun hasPlatform() : Boolean {
        return (configuration.hasPlatformModule() && !TextUtils.isEmpty(configuration.webUrl))
    }

    fun getDisableDialogTime(): Int {
        return configuration.disableTime
    }

    fun setAlarmMode(value: String) {
        configuration.alarmMode = value
    }

    fun getAlarmPendingTime(): Int {
        return configuration.pendingTime
    }

    fun getAlarmCode(): Int {
        return configuration.alarmCode
    }

    fun getAlarmMode(): String {
        return configuration.alarmMode
    }

    fun isAlarmDisableMode(): Boolean {
        return (getAlarmMode() == MODE_ARM_HOME
                || getAlarmMode() == MODE_ARM_AWAY
                || getAlarmMode() == MODE_HOME_TRIGGERED_PENDING
                || getAlarmMode() == MODE_AWAY_TRIGGERED_PENDING
                || getAlarmMode() == MODE_TRIGGERED_PENDING)
    }

    fun isArmed(value: Boolean) {
        armed = value
    }

    fun isArmed(): Boolean {
        return armed
    }

    fun clearMessages():Completable {
        return Completable.fromAction {
            messageDataSource.deleteAllMessages()
        }
    }

    /**
     * Get the messages.
     * @return a [Flowable] that will emit every time the messages have been updated.
     */
    fun getMessages():Flowable<List<Message>> {
        return dataSource.getMessages()
                .filter {messages -> messages.isNotEmpty()}
    }

    fun getAlarmState():Flowable<String> {
        return dataSource.getMessages(ALARM_TYPE)
                .filter {messages -> messages.isNotEmpty()}
                .map {messages -> messages[messages.size - 1]}
                .map {message ->
                    Timber.d("state: " + message.payload)
                    setAlarmModeFromState(message.payload!!)
                    message.payload
                }
    }

    init {

    }

    public override fun onCleared() {
        //prevents memory leaks by disposing pending observable objects
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }
}