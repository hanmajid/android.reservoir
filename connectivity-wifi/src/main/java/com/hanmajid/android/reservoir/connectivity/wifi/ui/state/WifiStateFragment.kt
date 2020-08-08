package com.hanmajid.android.reservoir.connectivity.wifi.ui.state

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
    private var errors = mutableMapOf(
        ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK to false,
        ERROR_API_LEVEL_P2P_SUPPORTED_STATUS to false,
        ERROR_PERMISSION_DENIED to false,
        ERROR_LOCATION_DISABLED to false
    )
    private var messages = mapOf(
        ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK to R.string.wifi_state_error_api_level_register_network_callback,
        ERROR_API_LEVEL_P2P_SUPPORTED_STATUS to R.string.wifi_state_error_api_level_p2p_supported_status,
        ERROR_PERMISSION_DENIED to R.string.wifi_state_error_permission_denied,
        ERROR_LOCATION_DISABLED to R.string.wifi_state_error_location_disabled
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWifiStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup bindings
        setupBinding()
        // Setup connection and Wi-Fi state listeners
        setupWifiState()
        // Intialize UI
        uiUpdateAll()
    }

    private fun setupBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.navController = findNavController()

        val contract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            uiUpdateAll()
        }
        binding.buttonWifiSettings.setOnClickListener {
            contract.launch(
                Intent(
                    Settings.ACTION_WIFI_SETTINGS
                )
            )
        }
        binding.buttonLocationSettings.setOnClickListener {
            contract.launch(
                Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
            )
        }
        binding.buttonPermissionSettings.setOnClickListener {
            contract.launch(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
            )
        }
        binding.swipeRefresh.setOnRefreshListener {
            uiUpdateAll()
        }
    }

    private fun setupWifiState() {
        // Register listener for network changes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                NetworkChangesCallback(requireContext())
            )
        } else {
            errors[ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK] = true
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

    private fun uiUpdateAll() {
        uiUpdateWifiState()
        uiUpdateConnectedWifi()
    }

    private fun uiUpdateWifiState(state: Int = wifiManager.wifiState) {
        binding.isWifiEnabled =
            state == WifiManager.WIFI_STATE_ENABLING || state == WifiManager.WIFI_STATE_ENABLED
        binding.wifiState = WifiStateUtil.getWifiStateDescription(requireContext(), state)
    }

    private fun uiUpdateConnectedWifi() {
        PermissionUtil.requestPermissionIfNeeded(
            this,
            REQUIRED_PERMISSION,
            getString(R.string.wifi_state_location_permission_rationale)
        ) { isGranted ->
            binding.swipeRefresh.isRefreshing = false

            binding.isPermissionGranted = isGranted == true
            errors[ERROR_PERMISSION_DENIED] = isGranted != true

            // Update location service status.
            val isLocationEnabled = LocationManagerCompat.isLocationEnabled(locationManager)
            binding.isLocationEnabled = isLocationEnabled
            errors[ERROR_LOCATION_DISABLED] = !isLocationEnabled

            // Update connected Wi-Fi info.
            val wifiInfo = wifiManager.connectionInfo
            binding.connectedWifi =
                if (wifiInfo.ssid != "<unknown ssid>") wifiInfo.ssid else "-"

            uiUpdateErrorMessages()
        }

        // Check P2P support each time connection changes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.p2pSupportedStatus =
                getString(
                    if (wifiManager.isP2pSupported) R.string.supported else R.string.not_supported
                )
        } else {
            binding.p2pSupportedStatus =
                getString(R.string.wifi_state_error_api_level_p2p_supported_status)
            errors[ERROR_API_LEVEL_P2P_SUPPORTED_STATUS] = true
        }
    }

    private fun uiUpdateErrorMessages() {
        val activeErrors = errors.filter {
            it.value
        }.keys.toList()

        binding.errorMessage = messages.filter {
            activeErrors.contains(it.key)
        }.values.toList().joinToString("\n") {
            getString(it)
        }
    }

    companion object {
        private const val TAG = "WifiStateFragment"
        private const val REQUIRED_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION

        private const val ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK = -1
        private const val ERROR_API_LEVEL_P2P_SUPPORTED_STATUS = -2
        private const val ERROR_PERMISSION_DENIED = -3
        private const val ERROR_LOCATION_DISABLED = -4
    }
}