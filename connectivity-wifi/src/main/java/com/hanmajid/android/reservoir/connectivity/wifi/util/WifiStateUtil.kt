package com.hanmajid.android.reservoir.connectivity.wifi.util

import android.content.Context
import android.net.wifi.WifiManager
import com.hanmajid.android.reservoir.connectivity.wifi.R

class WifiStateUtil {

    companion object {

        @JvmStatic
        fun getWifiStateDescription(context: Context, state: Int) = when (state) {
            WifiManager.WIFI_STATE_DISABLED -> context.getString(R.string.wifi_state_disabled)
            WifiManager.WIFI_STATE_DISABLING -> context.getString(R.string.wifi_state_disabling)
            WifiManager.WIFI_STATE_ENABLED -> context.getString(R.string.wifi_state_enabled)
            WifiManager.WIFI_STATE_ENABLING -> context.getString(R.string.wifi_state_enabling)
            WifiManager.WIFI_STATE_UNKNOWN -> context.getString(R.string.wifi_state_unknown)
            else -> "-"
        }
    }
}