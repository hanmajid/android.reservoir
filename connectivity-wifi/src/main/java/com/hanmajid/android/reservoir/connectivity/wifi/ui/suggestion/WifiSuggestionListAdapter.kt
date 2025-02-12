package com.hanmajid.android.reservoir.connectivity.wifi.ui.suggestion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hanmajid.android.reservoir.connectivity.wifi.data.MyWifiSuggestion
import com.hanmajid.android.reservoir.connectivity.wifi.databinding.ItemWifiSuggestionBinding

class WifiSuggestionListAdapter(
    private val onClickSuggest: (MyWifiSuggestion) -> Unit,
    private val onClickRemove: (MyWifiSuggestion) -> Unit
) :
    ListAdapter<MyWifiSuggestion, RecyclerView.ViewHolder>(WIFI_COMPARATOR) {

    companion object {
        private val WIFI_COMPARATOR = object : DiffUtil.ItemCallback<MyWifiSuggestion>() {
            override fun areItemsTheSame(
                oldItem: MyWifiSuggestion,
                newItem: MyWifiSuggestion
            ): Boolean {
                return oldItem.ssid == newItem.ssid
            }

            override fun areContentsTheSame(
                oldItem: MyWifiSuggestion,
                newItem: MyWifiSuggestion
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WifiSuggestionViewHolder).bind(
            getItem(position),
            onClickSuggest,
            onClickRemove
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WifiSuggestionViewHolder.create(
            parent
        )
    }

    class WifiSuggestionViewHolder(val binding: ItemWifiSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            wifiNetworkSuggestion: MyWifiSuggestion?,
            onClickSuggest: (MyWifiSuggestion) -> Unit,
            onClickRemove: (MyWifiSuggestion) -> Unit
        ) {
            wifiNetworkSuggestion?.apply {
                binding.suggestion = this
                binding.buttonSuggest.setOnClickListener {
                    onClickSuggest(this)
                }
                binding.buttonRemove.setOnClickListener {
                    onClickRemove(this)
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup): WifiSuggestionViewHolder {
                val binding = ItemWifiSuggestionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return WifiSuggestionViewHolder(
                    binding
                )
            }
        }
    }
}