/*
 * Copyright (c) 2018. ThanksMister LLC
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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() {

    private var listener: SettingsFragmentListener? = null

    interface SettingsFragmentListener {
        fun navigatePageNumber(page:Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is SettingsFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement SettingsFragmentListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonDeviceSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.DEVICE_SETTINGS) }
        buttonAlarmSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.ALARM_SETTINGS) }
        buttonMqttSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.MQTT_SETTINGS) }
        buttonNotificationSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.NOTIFICATIONS_SETTINGS) }
        buttonCameraSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.CAMERA_SETTINGS) }
        buttonScreenSaverSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.SCREEN_SAVER_SETTINGS) }
        buttonWeatherSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.WEATHER_SETTINGS) }
        buttonPlatformSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.PLATFORM_SETTINGS) }
        buttonAboutSettings.setOnClickListener { listener?.navigatePageNumber(SettingsActivity.ABOUT_SETTINGS) }
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}