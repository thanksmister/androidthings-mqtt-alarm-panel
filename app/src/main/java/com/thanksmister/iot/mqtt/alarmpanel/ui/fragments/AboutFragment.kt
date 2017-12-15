/*
 * Copyright (c) 2017 ThanksMister LLC
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

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import timber.log.Timber

class AboutFragment : BaseFragment() {

    private var versionNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(fragmentView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(fragmentView, savedInstanceState)

        try {
            val packageInfo = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
            val versionName = activity!!.findViewById<TextView>(R.id.versionName)
            versionNumber = " v" + packageInfo.versionName
            versionName.text = versionNumber
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e.message)
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        val GOOGLE_PLAY_RATING = "com.thanksmister.iot.mqtt.alarmpanel"
        val GITHUB_URL = "https://github.com/thanksmister/android-mqtt-alarm-panel"
        val EMAIL_ADDRESS = "mister@thanksmister.com"

        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}