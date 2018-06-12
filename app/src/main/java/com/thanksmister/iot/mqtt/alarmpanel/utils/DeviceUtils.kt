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

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.text.TextUtils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Calendar
import java.util.Date
import java.util.UUID

import timber.log.Timber

object DeviceUtils {

    val uuIdHash: String
        get() {
            var deviceId: String? = null
            try {
                Timber.d("Fetching Android ID")
                deviceId = UUID.randomUUID().toString()
            } catch (e: Exception) {
                Timber.e("ANDROID_ID Error: " + e.message)
            }

            if (TextUtils.isEmpty(deviceId)) {
                deviceId = System.currentTimeMillis().toString()
            }

            return md5(deviceId!!)
        }

    private fun convertScreenBrightnessToFloat(currentBrightness: Int): Float {
        return (currentBrightness.toFloat()/10)
    }

    /**
     * Returns the adjusted brightness depending when its night mode
     */
    fun getScreenBrightnessNightMode(currentBrightness: Int): Float {
        val brightness = convertScreenBrightnessToFloat(currentBrightness)
        if(brightness/2 <= 0f) {
            return .05f
        }
        return brightness/2
    }

    /**
     * Returns the adjusted brightness depending on the time of day (less bright at night)
     * @param currentBrightness
     * @return
     */
    fun getScreenBrightnessBasedOnDayTime(currentBrightness: Int, startTime: Float, endTime: Float): Float {
        val brightness = convertScreenBrightnessToFloat(currentBrightness)
        if (brightness == 1f) {
            return .05f
        }
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (hourOfDay >= startTime || hourOfDay < endTime) {
            brightness/2
        } else  {
            brightness
        }
    }

    // https://stackoverflow.com/questions/4846484/md5-hashing-in-android
    private fun md5(s: String): String {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest = MessageDigest
                    .getInstance(MD5)
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF + aMessageDigest)
                while (h.length < 2)
                    h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return UUID.randomUUID().toString()
    }

    /*
    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return UUID.randomUUID().toString();
    }
     */
}