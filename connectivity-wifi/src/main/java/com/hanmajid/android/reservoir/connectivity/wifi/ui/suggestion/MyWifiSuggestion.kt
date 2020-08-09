package com.hanmajid.android.reservoir.connectivity.wifi.ui.suggestion

import android.net.wifi.WifiNetworkSuggestion
import android.os.Build

/**
 * Custom model for Wi-Fi suggestion is needed because WifiNetworkSuggestion'
 * properties are private.
 */
data class MyWifiSuggestion(
    val ssid: String,
    val bssid: String? = null,
    val isAppInteractionRequired: Boolean? = null,
    val priority: Int? = null,
    val wpa2Passphrase: String? = null,
    val wpa3Passphrase: String? = null
) {
    val hasPassword: Boolean = wpa2Passphrase != null || wpa3Passphrase != null

    fun toNetworkSuggestion(): WifiNetworkSuggestion? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val builder = WifiNetworkSuggestion.Builder().apply {
                setSsid(ssid)
                wpa2Passphrase?.let {
                    setWpa2Passphrase(it)
                }
                wpa3Passphrase?.let {
                    setWpa3Passphrase(it)
                }
                isAppInteractionRequired?.let {
                    setIsAppInteractionRequired(it)
                }
            }
            return builder.build()
        } else {
            return null
        }
    }
}