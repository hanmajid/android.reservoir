package com.hanmajid.android.reservoir.connectivity.wifi.ui.p2p

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hanmajid.android.reservoir.common.util.PermissionUtil
import com.hanmajid.android.reservoir.connectivity.wifi.R
import com.hanmajid.android.reservoir.connectivity.wifi.databinding.FragmentWifiP2pBinding
import com.hanmajid.android.reservoir.connectivity.wifi.util.WifiP2pUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WifiP2pFragment : Fragment() {

    private lateinit var binding: FragmentWifiP2pBinding
    private lateinit var adapter: WifiP2pListAdapter

    private val locationManager: LocationManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val wifiManager: WifiManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val wifiP2pManager: WifiP2pManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private var wifiP2pChannel: WifiP2pManager.Channel? = null
    private var isWifiP2pEnabled: Boolean = false
    private var host: String? = ""
    private var serverSocket: ServerSocket? = null
    private var receiveFileDialog: AlertDialog? = null

    private var errors = mutableMapOf(
        ERROR_WIFI_DISABLED to false,
        ERROR_PERMISSION_DENIED to false,
        ERROR_LOCATION_DISABLED to false
    )
    private var messages = mapOf(
        ERROR_WIFI_DISABLED to R.string.wifi_p2p_error_wifi_disabled,
        ERROR_PERMISSION_DENIED to R.string.wifi_state_error_permission_denied,
        ERROR_LOCATION_DISABLED to R.string.wifi_state_error_location_disabled
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWifiP2pBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup bindings
        setupBinding()
        // Setup Wi-Fi P2P listener
        setupWifiP2p()
        // Initialize UI
        uiUpdateAll()
    }

    private fun setupBinding() {
        binding.lifecycleOwner = viewLifecycleOwner

        val intentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                uiUpdateAll()
            }
        binding.buttonWifiSettings.setOnClickListener {
            intentLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        binding.buttonLocationSettings.setOnClickListener {
            intentLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        binding.buttonPermissionSettings.setOnClickListener {
            intentLauncher.launch(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
            )
        }

        binding.swipeRefresh.setOnRefreshListener {
            uiUpdateAll()
        }
    }

    private fun setupWifiP2p() {
        wifiP2pChannel = wifiP2pManager.initialize(requireContext(), Looper.getMainLooper(), null)
        wifiP2pChannel?.also { channel ->
            WifiP2pBroadcastReceiver(
                wifiP2pManager,
                channel,
                requireActivity(),
                viewLifecycleOwner,
                { state ->
                    // Wi-Fi state changes
                    isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    uiUpdateWifiState()
                },
                {
                    // Wi-Fi P2P peers changes
                    adapter.submitList(it.deviceList.toList())
                    binding.swipeRefresh.isRefreshing = false
                },
                { info, _ ->
                    // Wi-Fi P2P connection changes
                    info?.let { info ->
                        if (info.groupFormed) {
                            host = info.groupOwnerAddress.hostAddress
                            adapter.isGroupOwner = info.isGroupOwner
                        }
                    }
                }
            )
        }

        adapter = WifiP2pListAdapter(
            wifiP2pManager,
            wifiP2pChannel,
            {
                // On click send file
                // Allow user to pick an image from Gallery or other
                // registered apps
                val intentLauncher =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        val uri = it.data?.data
                        host?.apply {
                            uri?.let { uri ->
                                sendFile(
                                    this, uri
                                ) { message ->
                                    message?.let { message ->
                                        Snackbar.make(
                                            binding.root, message, Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                intentLauncher.launch(
                    Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "image/*"
                    }
                )
            },
            {
                // On click receive file
                startServer { file, message ->
                    message?.let {
                        receiveFileDialog?.dismiss()
                        Snackbar.make(
                            binding.root, it, Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction("See File") {
                                val fileUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    FILE_PROVIDER_AUTHORITY,
                                    file!!
                                )
                                val intent = Intent()
                                intent.action = Intent.ACTION_VIEW
                                intent.setDataAndType(fileUri, "image/*")
                                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                requireContext().startActivity(intent)
                            }
                            .show()
                    }
                }
                receiveFileDialog = MaterialAlertDialogBuilder(requireContext())
                    .setMessage(getString(R.string.waiting_for_file))
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        closeServer()
                    }
                    .show()
            }, { manager, channel, device ->
                // On click connect
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.wifi_p2p_connect, device.deviceName))
                    .setNegativeButton(R.string.cancel) { _, _ ->
                    }
                    .setPositiveButton(R.string.connect) { _, _ ->
                        val config = WifiP2pConfig()
                        config.deviceAddress = device.deviceAddress
                        channel?.also { channel ->
                            manager?.connect(
                                channel,
                                config,
                                object : WifiP2pManager.ActionListener {
                                    override fun onSuccess() {}
                                    override fun onFailure(reason: Int) {}
                                }
                            )
                        }
                    }
                    .show()
            },
            { manager, channel, device ->
                // On click disconnect
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.wifi_p2p_disconnect, device.deviceName))
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .setPositiveButton(R.string.disconnect) { _, _ ->
                        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {}
                            override fun onFailure(reason: Int) {}
                        })
                    }
                    .show()
            }, { manager, channel, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    manager?.createGroup(
                        channel!!,
                        object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {
                                manager.requestGroupInfo(
                                    channel,
                                    WifiP2pManager.GroupInfoListener { group ->
                                        group?.let {
                                            Log.wtf(TAG, it.toString())
                                        }
                                    })
                            }

                            override fun onFailure(reason: Int) {
                                Log.wtf(
                                    TAG,
                                    "Failed: ${WifiP2pUtil.getWifiP2PFailureReason(reason)}"
                                )
                            }

                        }
                    )
                } else {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.create_group_error_api_level),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        binding.recyclerView.adapter = adapter
    }

    private fun uiUpdateAll() {
        uiUpdateWifiState()
        uiUpdateP2pList()
    }

    private fun uiUpdateWifiState(): Boolean {
        val isWifiEnabled = wifiManager.wifiState == WifiManager.WIFI_STATE_ENABLED
        binding.isWifiEnabled = isWifiEnabled && isWifiP2pEnabled
        errors[ERROR_WIFI_DISABLED] = !(isWifiEnabled && isWifiP2pEnabled)
        return isWifiEnabled
    }

    private fun uiUpdateP2pList() {
        binding.swipeRefresh.isRefreshing = true
        PermissionUtil.requestPermissionIfNeeded(
            this,
            REQUIRED_PERMISSION,
            getString(R.string.wifi_p2p_location_permission_rationale)
        ) { isGranted ->
            val isWifiEnabled = uiUpdateWifiState()

            binding.isPermissionGranted = isGranted == true
            errors[ERROR_PERMISSION_DENIED] = isGranted != true

            val isLocationEnabled = LocationManagerCompat.isLocationEnabled(locationManager)
            binding.isLocationEnabled = isLocationEnabled
            errors[ERROR_LOCATION_DISABLED] = !isLocationEnabled

            if (isWifiEnabled && isGranted == true && isLocationEnabled) {
                adapter.submitList(emptyList())
                wifiP2pManager.discoverPeers(
                    wifiP2pChannel,
                    object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {}

                        override fun onFailure(reason: Int) {
                            binding.swipeRefresh.isRefreshing = false
                        }
                    })
            } else {
                binding.swipeRefresh.isRefreshing = false
            }

            uiUpdateErrorMessages()
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

    private fun sendFile(
        host: String,
        uri: Uri,
        onComplete: (message: String?) -> Unit
    ) {
        Log.wtf(TAG, "Sending file... $host $GROUP_OWNER_PORT")
        val socket = Socket()
        var len: Int?
        val buf = ByteArray(1024)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                socket.bind(null)
                socket.connect((InetSocketAddress(host, GROUP_OWNER_PORT)), 5000)

                /**
                 * Create a byte stream from a file and pipe it to the output stream
                 * of the socket. This data is retrieved by the server device.
                 */
                val outputStream = socket.getOutputStream()
                val cr = requireContext().contentResolver
                val inputStream = cr.openInputStream(uri)
                while (inputStream?.read(buf).also { len = it } != -1) {
                    outputStream.write(buf, 0, len!!)
                }
                outputStream.close()
                inputStream?.close()
                onComplete(getString(R.string.file_sent))
            } catch (e: IOException) {
                Log.wtf(TAG, e.message)
                onComplete(e.message)
            } catch (e: Exception) {
                Log.wtf(TAG, e.message)
                onComplete(e.message)
            } finally {
                /**
                 * Clean up any open sockets when done
                 * transferring or if an exception occurred.
                 */
                socket.takeIf { it.isConnected }?.apply {
                    close()
                }
            }
        }
    }

    private fun startServer(onComplete: (file: File?, error: String?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            val port = GROUP_OWNER_PORT
            Log.wtf(TAG, "Starting server... $port")
            /**
             * Create a server socket.
             */
            try {
                serverSocket = ServerSocket()
                serverSocket?.reuseAddress = true
                serverSocket?.bind(InetSocketAddress(port))
                serverSocket.use {
                    /**
                     * Wait for client connections. This call blocks until a connection
                     * is accepted from a client.
                     */
                    val client = serverSocket!!.accept()

                    /**
                     * If this code is reached, a client has connected and transferred data
                     * Save the input stream from the client.
                     */
                    val filename = "${System.currentTimeMillis()}.jpg"
                    val f = File(
                        requireContext().getExternalFilesDir(DIRECTORY_FOLDER_RECEIVED), filename
                    )
                    f.createNewFile()
                    val inputStream = client.getInputStream()
                    val out = FileOutputStream(f)

                    // Copy file
                    val buf = ByteArray(1024)
                    var len: Int
                    try {
                        while (inputStream.read(buf).also { len = it } != -1) {
                            out.write(buf, 0, len)
                        }
                        out.close()
                        inputStream.close()
                    } catch (e: IOException) {

                    }
                    serverSocket!!.close()
                    Log.wtf(TAG, "File received: $filename")
                    onComplete(f, getString(R.string.file_received, filename))
                }
            } catch (e: IOException) {
                Log.wtf(TAG, e.message)
                onComplete(null, null)
            } catch (e: Exception) {
                Log.wtf(TAG, e.message)
                onComplete(null, null)
            }
        }
    }

    private fun closeServer() {
        serverSocket?.close()
    }

    companion object {
        private const val TAG = "WifiP2pFragment"
        private const val REQUIRED_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION
        private const val FILE_PROVIDER_AUTHORITY =
            "com.hanmajid.reservoir.connectivity.wifi.p2p.fileprovider"
        private const val DIRECTORY_FOLDER_RECEIVED = "received"

        private const val GROUP_OWNER_PORT = 8888

        private const val ERROR_WIFI_DISABLED = -1
        private const val ERROR_PERMISSION_DENIED = -2
        private const val ERROR_LOCATION_DISABLED = -3
    }
}