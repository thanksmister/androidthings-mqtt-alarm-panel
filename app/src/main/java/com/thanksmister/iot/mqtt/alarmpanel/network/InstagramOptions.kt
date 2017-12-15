package com.thanksmister.iot.mqtt.alarmpanel.network

import android.text.TextUtils

import dpreference.DPreference

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class InstagramOptions private constructor(
        /**
         * Preferences.
         */
        private val sharedPreferences: DPreference) {

    /**
     * Fit image to screen.
     */
    private var fitScreen: Boolean = false

    /**
     * Image source.
     */
    private var imageSource: String? = null

    /**
     * Rotation interval.
     */
    private var rotation: Int = 0

    val isValid: Boolean
        get() = !TextUtils.isEmpty(imageSource)

    var imageRotation: Int
        get() = rotation
        set(value) = this.sharedPreferences.setPrefInt(PREF_IMAGE_ROTATION, value)

    var imageFitScreen: Boolean
        get() = fitScreen
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_IMAGE_FIT_SIZE, value)

    fun getImageSource(): String? {
        return imageSource
    }

    fun setImageSource(value: String) {
        this.sharedPreferences.setPrefString(PREF_IMAGE_SOURCE, value)
    }

    private fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(IMAGE_OPTIONS_UPDATED, value)
    }

    fun hasUpdates(): Boolean {
        val updates = sharedPreferences.getPrefBoolean(IMAGE_OPTIONS_UPDATED, false)
        if (updates) {
            setOptionsUpdated(false)
        }
        return updates
    }

    companion object {

        val PREF_IMAGE_SOURCE = "pref_image_source"
        val PREF_IMAGE_FIT_SIZE = "pref_image_fit"
        val PREF_IMAGE_ROTATION = "pref_image_rotation"

        private val IMAGE_OPTIONS_UPDATED = "pref_image_options_updated"
        private val ROTATE_TIME_IN_MINUTES = 30 // 30 minutes

        /**
         * Construct a MqttOptions object from Configuration.
         */
        fun from(sharedPreferences: DPreference): InstagramOptions {
            try {
                val options = InstagramOptions(sharedPreferences)
                options.imageSource = sharedPreferences.getPrefString(PREF_IMAGE_SOURCE, "omjsk")
                options.fitScreen = sharedPreferences.getPrefBoolean(PREF_IMAGE_FIT_SIZE, false)
                options.rotation = sharedPreferences.getPrefInt(PREF_IMAGE_ROTATION, ROTATE_TIME_IN_MINUTES)
                return options
            } catch (e: Exception) {
                throw IllegalArgumentException("While processing image options", e)
            }

        }
    }
}