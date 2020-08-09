package com.hanmajid.android.reservoir.connectivity.wifi.util

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import com.hanmajid.android.reservoir.connectivity.wifi.R
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_SIGNAL_LEVEL_AVERAGE
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_SIGNAL_LEVEL_BAD
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_SIGNAL_LEVEL_GOOD
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_SIGNAL_LEVEL_VERY_BAD
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_SIGNAL_LEVEL_VERY_GOOD

class WifiScanUtil {

    companion object {
        /**
         * Requires CHANGE_WIFI_STATE and ACCESS_FINE_LOCATION permission.
         */
        @JvmStatic
        fun startScanning(
            wifiManager: WifiManager,
            distinctSSID: Boolean,
            onFailed: (scanResults: List<ScanResult>) -> Unit
        ) {
            val success = wifiManager.startScan()
            if (!success) {
                onFailed(getScanResults(wifiManager, distinctSSID))
            }
        }

        /**
         * Requires ACCESS_WIFI_STATE and ACCESS_FINE_LOCATION permission.
         */
        @JvmStatic
        fun getScanResults(wifiManager: WifiManager, distinctSSID: Boolean): List<ScanResult> {
            return if (distinctSSID) filterDistinctSSID(wifiManager.scanResults) else wifiManager.scanResults
        }

        @JvmStatic
        fun filterDistinctSSID(scanResults: List<ScanResult>): List<ScanResult> {
            val distinctResults = mutableMapOf<String, ScanResult>()
            scanResults.forEach {
                if (!distinctResults.contains(it.SSID)) {
                    distinctResults[it.SSID] = it
                } else if (distinctResults[it.SSID]!!.level < it.level) {
                    distinctResults[it.SSID] = it
                }
            }
            return distinctResults.values.toList()
        }

        @JvmStatic
        fun getWifiSignalLevelDescription(levelNum: Int, context: Context) = when (levelNum) {
            WIFI_SIGNAL_LEVEL_VERY_BAD -> context.getString(R.string.wifi_scan_signal_level_very_bad)
            WIFI_SIGNAL_LEVEL_BAD -> context.getString(R.string.wifi_scan_signal_level_bad)
            WIFI_SIGNAL_LEVEL_AVERAGE -> context.getString(R.string.wifi_scan_signal_level_average)
            WIFI_SIGNAL_LEVEL_GOOD -> context.getString(R.string.wifi_scan_signal_level_good)
            WIFI_SIGNAL_LEVEL_VERY_GOOD -> context.getString(R.string.wifi_scan_signal_level_very_good)
            else -> "-"
        }
    }
}