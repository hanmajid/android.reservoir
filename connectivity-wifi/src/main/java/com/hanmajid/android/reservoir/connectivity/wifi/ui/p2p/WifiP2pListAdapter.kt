package com.hanmajid.android.reservoir.connectivity.wifi.ui.p2p

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hanmajid.android.reservoir.connectivity.wifi.WifiConstants
import com.hanmajid.android.reservoir.connectivity.wifi.databinding.ItemWifiP2pBinding

class WifiP2pListAdapter(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel?,
    private val onClickSend: () -> Unit,
    private val onClickReceive: () -> Unit,
    private val onClickConnect: (WifiP2pManager?, WifiP2pManager.Channel?, WifiP2pDevice) -> Unit,
    private val onClickDisconnect: (WifiP2pManager?, WifiP2pManager.Channel?, WifiP2pDevice) -> Unit,
    private val onClickCreateGroup: (WifiP2pManager?, WifiP2pManager.Channel?, WifiP2pDevice) -> Unit
) :
    ListAdapter<WifiP2pDevice, RecyclerView.ViewHolder>(WIFI_COMPARATOR) {

    var isGroupOwner: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    companion object {
        private val WIFI_COMPARATOR = object : DiffUtil.ItemCallback<WifiP2pDevice>() {
            override fun areItemsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice): Boolean {
                return oldItem.deviceAddress == newItem.deviceAddress
            }

            override fun areContentsTheSame(
                oldItem: WifiP2pDevice,
                newItem: WifiP2pDevice
            ): Boolean {
                return oldItem.deviceAddress == newItem.deviceAddress && oldItem.status == newItem.status
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WifiP2PDeviceViewHolder).bind(
            isGroupOwner,
            getItem(position),
            manager,
            channel,
            onClickSend,
            onClickReceive,
            onClickConnect,
            onClickDisconnect,
            onClickCreateGroup
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WifiP2PDeviceViewHolder.create(
            parent
        )
    }

    class WifiP2PDeviceViewHolder(val binding: ItemWifiP2pBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            isGroupOwner: Boolean,
            device: WifiP2pDevice?,
            manager: WifiP2pManager?,
            channel: WifiP2pManager.Channel?,
            onClickSend: () -> Unit,
            onClickReceive: () -> Unit,
            onClickConnect: (WifiP2pManager?, WifiP2pManager.Channel?, WifiP2pDevice) -> Unit,
            onClickDisconnect: (WifiP2pManager?, WifiP2pManager.Channel?, WifiP2pDevice) -> Unit,
            onClickCreateGroup: (WifiP2pManager?, WifiP2pManager.Channel?, WifiP2pDevice) -> Unit
        ) {
            device?.apply {
                binding.isGroupOwner = isGroupOwner
                binding.device = this
                binding.buttonConnect.setOnClickListener {
                    when (this.status) {
                        WifiConstants.WIFI_P2P_DEVICE_STATUS_AVAILABLE -> {
                            onClickConnect(manager, channel, this)
                        }
                    }
                }
                binding.buttonCreateGroup.setOnClickListener {
                    onClickCreateGroup(manager, channel, device)
                }
                binding.buttonSend.setOnClickListener {
                    onClickSend()
                }
                binding.buttonReceive.setOnClickListener {
                    onClickReceive()
                }
                binding.buttonDisconnect.setOnClickListener {
                    onClickDisconnect(manager, channel, this)
                }
            }
        }

        companion object {
            private const val TAG = "WifiP2pListAdapter"
            fun create(parent: ViewGroup): WifiP2PDeviceViewHolder {
                val binding = ItemWifiP2pBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return WifiP2PDeviceViewHolder(
                    binding
                )
            }
        }
    }
}