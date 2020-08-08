package com.hanmajid.android.reservoir.connectivity.wifi.ui.state

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkChangesCallback(
    val context: Context
) : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        sendBroadcast()
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        sendBroadcast()
    }

    private fun sendBroadcast() {
        context.sendBroadcast(
            Intent().apply {
                action = WifiStateBroadcastListener.WIFI_STATE_CONNECTION_CHANGES_ACTION
            }
        )
    }
}