package com.hanmajid.android.reservoir.connectivity.wifi.util

import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

class WifiStateUtil {

    companion object {

        @JvmStatic
        fun getWifiStateDescription(state: Int) = when (state) {
            WifiManager.WIFI_STATE_DISABLED -> "Disabled"
            WifiManager.WIFI_STATE_DISABLING -> "Disabling"
            WifiManager.WIFI_STATE_ENABLED -> "Enabled"
            WifiManager.WIFI_STATE_ENABLING -> "Enabling"
            WifiManager.WIFI_STATE_UNKNOWN -> "Unknown"
            else -> ""
        }

        /**
         * Requires [ACCESS_FINE_LOCATION] permission.
         */
        @JvmStatic
        fun getCurrentlyConnectedWifiInfo(wifiManager: WifiManager): WifiInfo {
            return wifiManager.connectionInfo
        }
    }
}