package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.graphics.Bitmap
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import android.widget.Toast
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkStatus
import androidx.work.Worker
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
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject
constructor(application: Application, private val dataSource: MessageDao, private val configuration: Configuration,
            private val mqttOptions: MQTTOptions) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance();
    private val disposable = CompositeDisposable()
    private val isNight = MutableLiveData<Boolean>()

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

    fun getIsNight(): LiveData<Boolean> {
        return isNight
    }

    fun setIsNight(night: Boolean) {
        this.isNight.value = night
    }

    fun hasPlatform() : Boolean {
        return (configuration.hasPlatformModule() && !TextUtils.isEmpty(configuration.webUrl))
    }

    fun hasCamera() : Boolean {
        return (configuration.hasCamera() && (configuration.hasMailGunCredentials() || configuration.hasTelegramCredentials()))
    }

    fun hasTss() : Boolean {
        return configuration.hasTssModule()
    }

    fun hasAlerts() : Boolean {
        return configuration.hasAlertsModule()
    }

    fun getAlarmMode(): String {
        return configuration.alarmMode
    }

    private fun setAlarmMode(value: String) {
        configuration.alarmMode = value
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

    /**
     * Insert new message into the database.
     */
    fun insertMessage(messageId: String, topic: String, payload: String): Completable {
        val type = when (topic) {
            mqttOptions.getCameraTopic() -> IMAGE_CAPTURE_TYPE
            mqttOptions.getNotificationTopic() -> NOTIFICATION_TYPE
            else -> ALARM_TYPE
        }
        return Completable.fromAction {
            val createdAt = DateUtils.generateCreatedAtDate()
            val message = Message()
            message.type = type
            message.topic = topic
            message.payload = payload
            message.messageId = messageId
            message.createdAt = createdAt
            dataSource.insertMessage(message)
        }
    }

    fun sendCapturedImage(bitmap: Bitmap) {
        if(configuration.hasMailGunCredentials()) {
            emailImage(bitmap)
        }
        if(configuration.hasTelegramCredentials()) {
            sendTelegram(bitmap)
        }
    }

    private fun sendTelegram(bitmap: Bitmap) {
        val token = configuration.telegramToken
        val chatId = configuration.telegramChatId
        val observable = Observable.create { emitter: ObservableEmitter<Any> ->
            val module = TelegramModule(getApplication())
            module.emailImage(token, chatId, bitmap, object : TelegramModule.CallbackListener {
                override fun onComplete() {
                    emitter.onNext(true)  // Pass on the data to subscriber
                }
                override fun onException(message: String?) {
                    emitter.onError(Throwable(message))
                }
            })
        }
        disposable.add(observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { Timber.d("Telegram Message posted successfully!"); }
                .doOnError({ throwable -> Timber.e("Telegram Message error: " + throwable.message); })
                .subscribe( ))
    }

    private fun emailImage(bitmap: Bitmap) {
        val domain = configuration.getMailGunUrl()
        val key = configuration.getMailGunApiKey()
        val from = configuration.getMailFrom()
        val to = configuration.getMailTo()
        val observable = Observable.create { emitter: ObservableEmitter<Any> ->
            val mailGunModule = MailGunModule(getApplication())
            val fromSubject = getApplication<Application>().getString(R.string.text_camera_image_subject, "<$from>")
            mailGunModule.emailImage(domain!!, key!!, fromSubject, to!!, bitmap, object : MailGunModule.CallbackListener {
                override fun onComplete() {
                    emitter.onNext(true)  // Pass on the data to subscriber
                }
                override fun onException(message: String?) {
                    emitter.onError(Throwable(message))
                }
            })
        }
        disposable.add(observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { Timber.d("Image posted successfully!"); }
                .doOnError({ throwable -> Timber.e("Image error: " + throwable.message); })
                .onErrorReturn { Toast.makeText(getApplication<Application>(), R.string.error_mailgun_credentials, Toast.LENGTH_LONG).show() }
                .subscribe( ))
    }

    public override fun onCleared() {
        //prevents memory leaks by disposing pending observable objects
        if (!disposable.isDisposed) {
            disposable.clear()
        }
        workManager.cancelAllWorkByTag(DAY_NIGHT_WORK_NAME);
    }

    /**
     * Network connectivity receiver to notify client of the network disconnect issues and
     * to clear any network notifications when reconnected. It is easy for network connectivity
     * to run amok that is why we only notify the user once for network disconnect with
     * a boolean flag.
     */
    companion object {
        const val DAY_NIGHT_WORK_NAME: String = "day_night_worker_tag"
    }
}