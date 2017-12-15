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
import javax.inject.Inject

class ScreenSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var modulePreference: CheckBoxPreference? = null
    private var photoSaverPreference: CheckBoxPreference? = null
    private var urlPreference: EditTextPreference? = null
    private var imageFitPreference: CheckBoxPreference? = null
    private var rotationPreference: EditTextPreference? = null
    private var inactivityPreference: ListPreference? = null
    private var imageOptions: InstagramOptions? = null

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

        modulePreference = findPreference(Configuration.PREF_MODULE_SAVER) as CheckBoxPreference
        photoSaverPreference = findPreference(Configuration.PREF_MODULE_PHOTO_SAVER) as CheckBoxPreference
        urlPreference = findPreference(Configuration.PREF_IMAGE_SOURCE) as EditTextPreference
        imageFitPreference = findPreference(Configuration.PREF_IMAGE_FIT_SIZE) as CheckBoxPreference
        rotationPreference = findPreference(Configuration.PREF_IMAGE_ROTATION) as EditTextPreference
        inactivityPreference = findPreference(Configuration.PREF_INACTIVITY_TIME) as ListPreference

        if (isAdded && activity != null) {
            imageOptions = (activity as BaseActivity).readImageOptions()
        }

        urlPreference!!.text = imageOptions!!.getImageSource()
        rotationPreference!!.text = imageOptions!!.imageRotation.toString()
        rotationPreference!!.summary = getString(R.string.preference_summary_image_rotation, imageOptions!!.imageRotation.toString())
        urlPreference!!.summary = getString(R.string.preference_summary_image_source, imageOptions!!.getImageSource())

        inactivityPreference!!.setDefaultValue(configuration.inactivityTime.toString())
        if (configuration.inactivityTime < SECONDS_VALUE) {
            inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_seconds,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        } else {
            inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_minutes,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        }

        modulePreference!!.isChecked = configuration.showScreenSaverModule()
        photoSaverPreference!!.isEnabled = configuration.showScreenSaverModule()
        photoSaverPreference!!.isChecked = configuration.showPhotoScreenSaver()
        imageFitPreference!!.isChecked = imageOptions!!.imageFitScreen
        urlPreference!!.isEnabled = configuration.showPhotoScreenSaver()
        imageFitPreference!!.isEnabled = configuration.showPhotoScreenSaver()
        rotationPreference!!.isEnabled = configuration.showPhotoScreenSaver()
        inactivityPreference!!.isEnabled = configuration.showScreenSaverModule()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {
            Configuration.PREF_MODULE_SAVER -> {
                val checked = modulePreference!!.isChecked
                configuration.setScreenSaverModule(checked)
                photoSaverPreference!!.isEnabled = checked
            }
            Configuration.PREF_MODULE_PHOTO_SAVER -> {
                val usePhotos = photoSaverPreference!!.isChecked
                configuration.setPhotoScreenSaver(usePhotos)
                urlPreference!!.isEnabled = usePhotos
                rotationPreference!!.isEnabled = usePhotos
                imageFitPreference!!.isEnabled = usePhotos
            }
            Configuration.PREF_IMAGE_SOURCE -> {
                val value = urlPreference!!.text
                imageOptions!!.setImageSource(value)
                urlPreference!!.summary = getString(R.string.preference_summary_image_source, value)
            }
            Configuration.PREF_IMAGE_FIT_SIZE -> {
                val fitScreen = imageFitPreference!!.isChecked
                imageOptions!!.imageFitScreen = fitScreen
            }
            Configuration.PREF_IMAGE_ROTATION -> {
                val rotation = Integer.valueOf(rotationPreference!!.text)!!
                imageOptions!!.imageRotation = rotation
                rotationPreference!!.summary = getString(R.string.preference_summary_image_rotation, rotation.toString())
            }
            Configuration.PREF_INACTIVITY_TIME -> {
                val inactivity = java.lang.Long.valueOf(inactivityPreference!!.value)!!
                configuration.inactivityTime = inactivity
                if (inactivity < SECONDS_VALUE) {
                    inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_seconds, DateUtils.convertInactivityTime(inactivity))
                } else {
                    inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_minutes, DateUtils.convertInactivityTime(inactivity))
                }
            }
        }
    }
}