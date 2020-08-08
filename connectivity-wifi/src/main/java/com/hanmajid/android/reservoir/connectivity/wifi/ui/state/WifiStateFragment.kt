package com.hanmajid.android.reservoir.connectivity.wifi.ui.state

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.location.LocationManagerCompat
import androidx.navigation.fragment.findNavController
import com.hanmajid.android.reservoir.common.util.PermissionUtil
import com.hanmajid.android.reservoir.connectivity.wifi.R
import com.hanmajid.android.reservoir.connectivity.wifi.databinding.FragmentWifiStateBinding
import com.hanmajid.android.reservoir.connectivity.wifi.util.WifiStateUtil

class WifiStateFragment : Fragment() {

    private lateinit var binding: FragmentWifiStateBinding

    private val wifiManager: WifiManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val connectivityManager: ConnectivityManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val locationManager: LocationManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWifiStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBinding()
        setupWifiState()
    }

    private fun setupBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.navController = findNavController()
    }

    private fun setupWifiState() {
        // Register listener for network changes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                NetworkChangesCallback(requireContext())
            )
        } else {

        }

        // Listen to Wi-Fi state changes here.
        WifiStateBroadcastListener(
            viewLifecycleOwner,
            requireContext(),
            { _, state ->
                // Wi-Fi state changes.
                uiUpdateWifiState(state)
            }, {
                // Connection state changes.
                uiUpdateConnectedWifi()
            }
        )
    }

    private fun uiUpdateWifiState(state: Int = wifiManager.wifiState) {
        binding.isWifiEnabled =
            state == WifiManager.WIFI_STATE_ENABLING || state == WifiManager.WIFI_STATE_ENABLED
        binding.wifiState = WifiStateUtil.getWifiStateDescription(state)
    }

    private fun uiUpdateConnectedWifi() {
        PermissionUtil.requestPermissionIfNeeded(
            this,
            REQUIRED_PERMISSION,
            "Allow location permission to see connected Wi-Fi info."
        ) {
            if (it == true) {
                val isLocationEnabled = LocationManagerCompat.isLocationEnabled(locationManager)
                binding.isLocationEnabled = isLocationEnabled
                val wifiInfo = WifiStateUtil.getCurrentlyConnectedWifiInfo(wifiManager)
                binding.connectedWifi =
                    if (wifiInfo.ssid != "<unknown ssid>") wifiInfo.ssid else "-"
            } else {
                // Location permission is denied.
            }
        }
    }

    companion object {
        private const val TAG = "WifiStateFragment"
        private val REQUIRED_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION
    }
}