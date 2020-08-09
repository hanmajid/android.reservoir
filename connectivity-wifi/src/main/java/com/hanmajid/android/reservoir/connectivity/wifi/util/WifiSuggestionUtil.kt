package com.hanmajid.android.reservoir.connectivity.wifi.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.fragment.app.Fragment
import com.hanmajid.android.reservoir.common.util.PermissionUtil
import com.hanmajid.android.reservoir.connectivity.wifi.R
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.STATUS_NETWORK_SUGGESTIONS_ERROR_API_LEVEL
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants.STATUS_NETWORK_SUGGESTIONS_ERROR_PERMISSIONS
import com.hanmajid.android.reservoir.connectivity.wifi.ui.suggestion.MyWifiSuggestion

class WifiSuggestionUtil {

    companion object {

        @JvmStatic
        fun addWifiNetworkSuggestions(
            fragment: Fragment,
            wifiManager: WifiManager,
            suggestions: List<MyWifiSuggestion>,
            onSuccess: (status: Int) -> Unit,
            onError: (status: Int) -> Unit
        ) {
            changeWifiNetworkSuggestions(
                WIFI_SUGGESTION_CHANGE_TYPE.ADD_SUGGESTION,
                fragment,
                wifiManager,
                suggestions,
                onSuccess,
                onError
            )
        }

        @JvmStatic
        fun removeWifiNetworkSuggestions(
            fragment: Fragment,
            wifiManager: WifiManager,
            suggestions: List<MyWifiSuggestion>,
            onSuccess: (status: Int) -> Unit,
            onError: (status: Int) -> Unit
        ) {
            changeWifiNetworkSuggestions(
                WIFI_SUGGESTION_CHANGE_TYPE.REMOVE_SUGGESTION,
                fragment,
                wifiManager,
                suggestions,
                onSuccess,
                onError
            )
        }

        @JvmStatic
        private fun changeWifiNetworkSuggestions(
            changeType: WIFI_SUGGESTION_CHANGE_TYPE,
            fragment: Fragment,
            wifiManager: WifiManager,
            suggestions: List<MyWifiSuggestion>,
            onSuccess: (status: Int) -> Unit,
            onError: (status: Int) -> Unit
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionUtil.requestPermissionsIfNeeded(
                    fragment,
                    REQUIRED_PERMISSIONS,
                    null
                ) {
                    val isGranted = it?.all {
                        it.value
                    }
                    if (isGranted == true) {
                        val wifiSuggestions = suggestions.map { it.toNetworkSuggestion() }
                        when (changeType) {
                            WIFI_SUGGESTION_CHANGE_TYPE.ADD_SUGGESTION -> {
                                onSuccess(wifiManager.addNetworkSuggestions(wifiSuggestions))
                            }
                            WIFI_SUGGESTION_CHANGE_TYPE.REMOVE_SUGGESTION -> {
                                onSuccess(wifiManager.removeNetworkSuggestions(wifiSuggestions))
                            }
                        }
                    } else {
                        // Permission is not granted.
                        onError(STATUS_NETWORK_SUGGESTIONS_ERROR_PERMISSIONS)
                    }
                }
            } else {
                // API level is not sufficient.
                onError(STATUS_NETWORK_SUGGESTIONS_ERROR_API_LEVEL)
            }
        }

        @JvmStatic
        fun getWifiSuggestionStatusDescription(status: Int, context: Context) = when (status) {
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL -> context.getString(R.string.wifi_suggestion_error_internal)
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED -> context.getString(R.string.wifi_suggestion_error_app_disallowed)
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE -> context.getString(R.string.wifi_suggestion_error_add_duplicate)
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP -> context.getString(
                R.string.wifi_suggestion_error_add_exceeds_max_per_app
            )
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_REMOVE_INVALID -> context.getString(R.string.wifi_suggestion_error_remove_invalid)
            STATUS_NETWORK_SUGGESTIONS_ERROR_PERMISSIONS -> context.getString(R.string.wifi_suggestion_error_permission_denied)
            STATUS_NETWORK_SUGGESTIONS_ERROR_API_LEVEL -> context.getString(R.string.wifi_suggestion_error_api_level)
            else -> "-"
        }

        val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CHANGE_WIFI_STATE)

        enum class WIFI_SUGGESTION_CHANGE_TYPE {
            ADD_SUGGESTION,
            REMOVE_SUGGESTION
        }
    }
}