package com.hanmajid.android.reservoir.connectivity.wifi.ui.scan

import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hanmajid.android.reservoir.connectivity.wifi.databinding.ItemWifiScanBinding

class WifiScanListAdapter : ListAdapter<ScanResult, RecyclerView.ViewHolder>(WIFI_SCAN_COMPARATOR) {

    var connectedWifiInfo: WifiInfo? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    companion object {
        private val WIFI_SCAN_COMPARATOR = object : DiffUtil.ItemCallback<ScanResult>() {
            override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem.BSSID == newItem.BSSID
            }

            override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem.BSSID == newItem.BSSID
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WifiViewHolder).bind(getItem(position), connectedWifiInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WifiViewHolder.create(
            parent
        )
    }

    class WifiViewHolder(val binding: ItemWifiScanBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(scanResult: ScanResult?, connectedWifiInfo: WifiInfo?) {
            scanResult?.apply {
                binding.isConnected = connectedWifiInfo?.bssid == this.BSSID
                binding.scanResult = this
            }
        }

        companion object {
            fun create(parent: ViewGroup): WifiViewHolder {
                val binding = ItemWifiScanBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return WifiViewHolder(
                    binding
                )
            }
        }
    }
}