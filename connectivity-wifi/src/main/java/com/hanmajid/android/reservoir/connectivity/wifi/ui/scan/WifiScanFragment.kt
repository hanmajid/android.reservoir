package com.hanmajid.android.reservoir.connectivity.wifi.ui.scan

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hanmajid.android.reservoir.common.util.PermissionUtil
import com.hanmajid.android.reservoir.connectivity.wifi.R
import com.hanmajid.android.reservoir.connectivity.wifi.databinding.FragmentWifiScanBinding
import com.hanmajid.android.reservoir.connectivity.wifi.ui.state.NetworkChangesCallback
import com.hanmajid.android.reservoir.connectivity.wifi.ui.state.WifiStateBroadcastListener
import com.hanmajid.android.reservoir.connectivity.wifi.util.WifiScanUtil

class WifiScanFragment : Fragment() {

    private lateinit var binding: FragmentWifiScanBinding

    private val wifiManager: WifiManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val connectivityManager: ConnectivityManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val locationManager: LocationManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val adapter = WifiScanListAdapter()

    private var errors = mutableMapOf(
        ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK to false,
        ERROR_PERMISSION_DENIED to false,
        ERROR_LOCATION_DISABLED to false
    )
    private var messages = mapOf(
        ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK to R.string.wifi_state_error_api_level_register_network_callback,
        ERROR_PERMISSION_DENIED to R.string.wifi_state_error_permission_denied,
        ERROR_LOCATION_DISABLED to R.string.wifi_state_error_location_disabled
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWifiScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup bindings
        setupBinding()
        // Setup Wi-Fi scanning & connection changes listeners
        setupWifiScan()
        // Initialize UI
        uiUpdateAll()
    }

    private fun setupBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.recyclerView.adapter = adapter

        val contract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                uiUpdateAll()
            }
        binding.buttonLocationSettings.setOnClickListener {
            contract.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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

    private fun setupWifiScan() {
        // Listen to Wi-Fi scan results.
        WifiScanBroadcastReceiver(
            viewLifecycleOwner,
            requireContext(),
            wifiManager,
            true,
            { scanResults ->
                // On scan success
                uiUpdateWifiScanResults(scanResults, true)
            }, { scanResults ->
                // On scan failed
                uiUpdateWifiScanResults(scanResults, false)
            }
        )

        // Listen to connected Wi-Fi changes to get connected Wi-Fi information.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                NetworkChangesCallback(requireContext())
            )
            WifiStateBroadcastListener(
                viewLifecycleOwner,
                requireContext(),
                { _, _ ->
                    // Do nothing
                }, {
                    // Connection state changes.
                    uiUpdateConnectedWifiInfo()
                }
            )
        } else {
            errors[ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK] = true
        }
    }

    private fun doWifiScan() {
        PermissionUtil.requestPermissionIfNeeded(
            this,
            REQUIRED_PERMISSION,
            getString(R.string.wifi_state_location_permission_rationale)
        ) { isGranted ->
            binding.isPermissionGranted = isGranted == true
            errors[ERROR_PERMISSION_DENIED] = isGranted != true

            // Update location service status.
            val isLocationEnabled = LocationManagerCompat.isLocationEnabled(locationManager)
            binding.isLocationEnabled = isLocationEnabled
            errors[ERROR_LOCATION_DISABLED] = !isLocationEnabled

            if (isLocationEnabled) {
                WifiScanUtil.startScanning(
                    wifiManager,
                    true
                ) { scanResults ->
                    // On scan failed
                    uiUpdateWifiScanResults(scanResults, false)
                }
            } else {
                binding.swipeRefresh.isRefreshing = false
            }

            uiUpdateErrorMessages()
        }
    }

    private fun uiUpdateAll() {
        binding.swipeRefresh.isRefreshing = true
        doWifiScan()
        uiUpdateConnectedWifiInfo()
    }

    private fun uiUpdateWifiScanResults(scanResults: List<ScanResult>, success: Boolean) {
        if (!success) {
            Snackbar.make(
                binding.root,
                getString(R.string.wifi_scan_error_throttling),
                Snackbar.LENGTH_SHORT
            )
                .setAction(getString(R.string.ok)) {}
                .show()
        }
        adapter.submitList(scanResults)
        binding.swipeRefresh.isRefreshing = false
    }

    private fun uiUpdateConnectedWifiInfo() {
        val wifiInfo = wifiManager.connectionInfo
        adapter.connectedWifiInfo = wifiInfo.takeIf { it.ssid != "<unknown ssid>" }
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
        private const val TAG = "WifiScanFragment"
        private const val REQUIRED_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION

        private const val ERROR_API_LEVEL_REGISTER_NETWORK_CALLBACK = -1
        private const val ERROR_PERMISSION_DENIED = -2
        private const val ERROR_LOCATION_DISABLED = -3
    }
}