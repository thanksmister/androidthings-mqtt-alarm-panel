/*
 * Copyright (c) 2017. ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import com.google.android.things.device.TimeManager
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_DEVICE_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_DEVICE_TIME_FORMAT
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_DEVICE_TIME_SERVER
import dagger.android.support.AndroidSupportInjection
import java.text.DateFormat
import java.util.*
import javax.inject.Inject


class DeviceSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var serverPreference: SwitchPreference? = null
    private var formatPreference: SwitchPreference? = null
    private var timePreference: Preference? = null
    private val timeManager = TimeManager()

    override fun onAttach(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Perform injection here for M (API 23) due to deprecation of onAttach(Activity).
            AndroidSupportInjection.inject(this)
        }
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey : String?) {
        addPreferencesFromResource(R.xml.preferences_device)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        timePreference = findPreference(PREF_DEVICE_TIME) as Preference
        timePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showTimePicker();
            true
        }

        serverPreference = findPreference(PREF_DEVICE_TIME_SERVER) as SwitchPreference
        formatPreference = findPreference(PREF_DEVICE_TIME_FORMAT) as SwitchPreference
        serverPreference!!.isChecked = configuration.useTimeServer

        val currentTimeString = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(Date())
        timePreference!!.summary = currentTimeString

        if(configuration.useTimeServer) {
            timeManager.setAutoTimeEnabled(configuration.useTimeServer)
        }
        if(configuration.timeFormat == TimeManager.FORMAT_12) {
            timeManager.setTimeFormat(TimeManager.FORMAT_12)
            formatPreference!!.summary = "1:00 PM"
            formatPreference!!.isChecked = false
        } else {
            timeManager.setTimeFormat(TimeManager.FORMAT_24)
            formatPreference!!.summary = "13:00"
            formatPreference!!.isChecked = true
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val mTimePicker: TimePickerDialog
        val is24Hour = (configuration.timeFormat == TimeManager.FORMAT_24)
        mTimePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener {
            timePicker, selectedHour, selectedMinute ->
            setTime(selectedHour, selectedMinute)
        }, hour, minute, is24Hour) // Yes 24 hour time
        mTimePicker.setTitle(getString(R.string.text_dialog_select_time))
        mTimePicker.show()
    }

    private fun setTime(selectedHour: Int, selectedMinute: Int ) {
        val calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        val timeStamp = calendar.timeInMillis;
        timeManager.setTime(timeStamp);
        val currentTimeString = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(Date())
        timePreference!!.summary = currentTimeString
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_DEVICE_TIME_FORMAT -> {
                if(formatPreference!!.isChecked) {
                    timeManager.setTimeFormat(TimeManager.FORMAT_24);
                    formatPreference!!.summary = "13:00"
                    configuration.timeFormat = TimeManager.FORMAT_24
                } else {
                    timeManager.setTimeFormat(TimeManager.FORMAT_12);
                    formatPreference!!.summary = "1:00 PM"
                    configuration.timeFormat = TimeManager.FORMAT_12
                }
            }
            PREF_DEVICE_TIME_SERVER -> {
                val checked = serverPreference!!.isChecked
                timeManager.setAutoTimeEnabled(checked)
                configuration.useTimeServer = checked
            }
        }
    }
}