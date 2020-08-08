package com.hanmajid.android.reservoir.connectivity.wifi.util

import android.net.wifi.WifiManager

class WifiSuggestionUtil {

    companion object {

        @JvmStatic
        fun getWifiSuggestionStatusDescription(status: Int) = when (status) {
            WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS -> "Success"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL -> "Error: Internal error :("
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED -> "Error: Please allow this app to control Wi-Fi"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE -> "Error: This suggestion is duplicate"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP -> "Error: Max suggestions is exceeded. Consider removing unused suggestions."
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_REMOVE_INVALID -> "Error: This suggestion doesn't exist."
            else -> ""
        }
    }
}