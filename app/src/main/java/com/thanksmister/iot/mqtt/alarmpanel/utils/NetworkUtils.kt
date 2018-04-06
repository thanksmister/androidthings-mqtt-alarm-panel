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

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import timber.log.Timber

/**
 * Network tools
 */
object NetworkUtils {

    private fun hasInternetAccess(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni == null
    }

    fun isConnectedToWifi(context: Context): Boolean {
        if (hasInternetAccess(context)) {
            return false
        }
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return mWifi.isConnected
    }

    fun isConnectionLte(context: Context): Boolean {
        return if (hasInternetAccess(context)) {
            false
        } else (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkType != TelephonyManager.NETWORK_TYPE_UNKNOWN
    }

    fun connectNetwork(context: Context, networkSSID: String?, networkPassword: String?) {
        Timber.d("connectNetwork")

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager;
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

        // don't connect if the currently connected network is the same
        val wifiInfo = wifiManager.connectionInfo
        if(wifiInfo.ssid == networkSSID) {
            return
        }

        Timber.d("WiFi connectToWifi")
        val conf = WifiConfiguration()
        conf.SSID = String.format("\"%s\"", networkSSID);
        conf.preSharedKey = String.format("\"%s\"", networkPassword);
        conf.status = WifiConfiguration.Status.ENABLED;

        val networkList = wifiManager.configuredNetworks
        var networkId = wifiManager.addNetwork(conf)
        if(networkId == -1) {
            networkId = getNetworkId(networkSSID, networkList)
        }

        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()
    }

    private fun getNetworkId(networkSSID: String?, networkList: List<WifiConfiguration>): Int {
        for (i in networkList) {
            if (i.SSID != null && i.SSID == "\"" + networkSSID + "\"") {
                return i.networkId
            }
        }
        return -1
    }

    /**
     * Get the current network name.
     */
     fun getCurrentNetworkName(context: Context): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager;
        val wifiInfo = wifiManager.connectionInfo
        val wirelessNetworkName = wifiInfo?.ssid
        var name = wirelessNetworkName?.replace("\"", "");
        if(TextUtils.isEmpty(name)){
            name = ""
        } else if (name!!.contains("unknown", true)) {
            return ""
        }
        return name
    }

}
