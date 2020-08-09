package com.hanmajid.android.reservoir.connectivity.wifi.ui.suggestion

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.location.LocationManagerCompat
import com.google.android.material.snackbar.Snackbar
import com.hanmajid.android.reservoir.common.util.PermissionUtil
import com.hanmajid.android.reservoir.connectivity.wifi.R
import com.hanmajid.android.reservoir.connectivity.wifi.data.MyWifiSuggestion
import com.hanmajid.android.reservoir.connectivity.wifi.databinding.FragmentWifiSuggestionBinding
import com.hanmajid.android.reservoir.connectivity.wifi.util.WifiSuggestionUtil

class WifiSuggestionFragment : Fragment() {

    private lateinit var binding: FragmentWifiSuggestionBinding
    private lateinit var adapter: WifiSuggestionListAdapter

    private val wifiManager: WifiManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val locationManager: LocationManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var errors = mutableMapOf(
        ERROR_API_LEVEL_REGISTER_BROADCAST_LISTENER to false,
        ERROR_PERMISSION_DENIED to false,
        ERROR_LOCATION_DISABLED to false
    )
    private var messages = mapOf(
        ERROR_API_LEVEL_REGISTER_BROADCAST_LISTENER to R.string.wifi_suggestion_error_api_level_register_broadcast_listener,
        ERROR_PERMISSION_DENIED to R.string.wifi_suggestion_listener_error_permission_denied,
        ERROR_LOCATION_DISABLED to R.string.wifi_suggestion_listener_error_location_disabled
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWifiSuggestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup bindings
        setupBinding()
        // Setup Wi-Fi suggestions
        setupWifiSuggestion()
        // Initialize UI
        uiUpdateAll()
    }

    private fun setupBinding() {
        binding.lifecycleOwner = viewLifecycleOwner

        val contract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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

    private fun setupWifiSuggestion() {
        WifiSuggestionConnectedBroadcastListener(
            viewLifecycleOwner,
            requireContext(),
            {
                // On success
                binding.isConnected = true
            },
            {
                // On error API level not sufficient.
                errors[ERROR_API_LEVEL_REGISTER_BROADCAST_LISTENER] = true
            }
        )

        adapter = WifiSuggestionListAdapter(
            { suggestion ->
                WifiSuggestionUtil.addWifiNetworkSuggestions(
                    this,
                    wifiManager,
                    listOf(suggestion),
                    {
                        uiShowSnackBarStatus(
                            it,
                            suggestion,
                            WifiSuggestionUtil.Companion.WifiSuggestionChangeType.ADD_SUGGESTION
                        )
                    },
                    {
                        uiShowSnackBarStatus(
                            it,
                            suggestion,
                            WifiSuggestionUtil.Companion.WifiSuggestionChangeType.ADD_SUGGESTION
                        )
                    }
                )
            },
            { suggestion ->
                WifiSuggestionUtil.removeWifiNetworkSuggestions(
                    this,
                    wifiManager,
                    listOf(suggestion),
                    {
                        uiShowSnackBarStatus(
                            it,
                            suggestion,
                            WifiSuggestionUtil.Companion.WifiSuggestionChangeType.REMOVE_SUGGESTION
                        )
                    },
                    {
                        uiShowSnackBarStatus(
                            it,
                            suggestion,
                            WifiSuggestionUtil.Companion.WifiSuggestionChangeType.REMOVE_SUGGESTION
                        )
                    }
                )
            }
        )
        binding.recyclerView.adapter = adapter
        val suggestion1 =
            MyWifiSuggestion(
                ssid = "hanmajid-wifi-password",
                priority = 1,
                wpa2Passphrase = "secret",
                isAppInteractionRequired = true
            )
        val suggestion2 =
            MyWifiSuggestion(
                ssid = "hanmajid-wifi-open",
                priority = 2,
                isAppInteractionRequired = false
            )
        val suggestions = listOf(suggestion1, suggestion2)
        adapter.submitList(suggestions)
    }

    private fun uiUpdateAll() {
        binding.swipeRefresh.isRefreshing = true
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

            uiUpdateErrorMessages()
        }
    }

    private fun uiShowSnackBarStatus(
        status: Int,
        suggestion: MyWifiSuggestion,
        type: WifiSuggestionUtil.Companion.WifiSuggestionChangeType
    ) {
        val message = when (status) {
            WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS -> {
                if (type == WifiSuggestionUtil.Companion.WifiSuggestionChangeType.ADD_SUGGESTION)
                    getString(R.string.wifi_suggestion_success_add, suggestion.ssid) else
                    getString(R.string.wifi_suggestion_success_remove, suggestion.ssid)
            }
            else -> WifiSuggestionUtil.getWifiSuggestionStatusDescription(status, requireContext())
        }

        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
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
        private const val TAG = "WifiSuggestionFragment"
        private const val REQUIRED_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION

        private const val ERROR_API_LEVEL_REGISTER_BROADCAST_LISTENER = -1
        private const val ERROR_PERMISSION_DENIED = -2
        private const val ERROR_LOCATION_DISABLED = -3
    }
}