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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.InstagramOptions
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils


import com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_screen_saver
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils.SECONDS_VALUE
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class ScreenSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var clockSaverPreference: CheckBoxPreference? = null
    private var inactivityPreference: ListPreference? = null

    override fun onAttach(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Perform injection here for M (API 23) due to deprecation of onAttach(Activity).
            AndroidSupportInjection.inject(this)
        }
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(preferences_screen_saver)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        clockSaverPreference = findPreference(Configuration.PREF_MODULE_CLOCK_SAVER) as CheckBoxPreference
        inactivityPreference = findPreference(Configuration.PREF_INACTIVITY_TIME) as ListPreference

        inactivityPreference!!.setDefaultValue(configuration.inactivityTime)
        inactivityPreference!!.value = configuration.inactivityTime.toString()

        if (configuration.inactivityTime < SECONDS_VALUE) {
            inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_seconds,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        } else {
            inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_minutes,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        }

        clockSaverPreference!!.isChecked = configuration.showClockScreenSaverModule()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {
            Configuration.PREF_MODULE_CLOCK_SAVER -> {
                val checked = clockSaverPreference!!.isChecked
                configuration.setClockScreenSaverModule(checked)
            }
            Configuration.PREF_INACTIVITY_TIME -> {
                val inactivity = inactivityPreference!!.value
                Timber.d("inactivity: " + inactivity)
                configuration.inactivityTime = inactivity.toLong()
                if (configuration.inactivityTime < SECONDS_VALUE) {
                    inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_seconds,
                            DateUtils.convertInactivityTime(configuration.inactivityTime))
                } else {
                    inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_minutes,
                            DateUtils.convertInactivityTime(configuration.inactivityTime))
                }
            }
        }
    }
}