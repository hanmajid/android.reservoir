package com.hanmajid.android.reservoir.connectivity.wifi.util

import android.net.wifi.p2p.WifiP2pManager
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_P2P_DEVICE_STATUS_AVAILABLE
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_P2P_DEVICE_STATUS_CONNECTED
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_P2P_DEVICE_STATUS_FAILED
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_P2P_DEVICE_STATUS_INVITED
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.WIFI_P2P_DEVICE_STATUS_UNAVAILABLE

class WifiP2pUtil {

    companion object {

        @JvmStatic
        fun getWifiP2PDeviceStatusDescription(status: Int) = when (status) {
            WIFI_P2P_DEVICE_STATUS_CONNECTED -> "Connected"
            WIFI_P2P_DEVICE_STATUS_INVITED -> "Invited"
            WIFI_P2P_DEVICE_STATUS_FAILED -> "Failed"
            WIFI_P2P_DEVICE_STATUS_AVAILABLE -> "Available"
            WIFI_P2P_DEVICE_STATUS_UNAVAILABLE -> "Unavailable"
            else -> "-"
        }

        @JvmStatic
        fun getWifiP2PFailureReason(reason: Int) = when (reason) {
            WifiP2pManager.ERROR -> "Error"
            WifiP2pManager.P2P_UNSUPPORTED -> "P2P unsupported"
            WifiP2pManager.BUSY -> "Busy"
            WifiP2pManager.NO_SERVICE_REQUESTS -> "No service requests"
            else -> ""
        }
    }
}