package com.hanmajid.android.reservoir.connectivity.wifi.ui.suggestion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hanmajid.android.reservoir.connectivity.wifi.R

class WifiSuggestionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_suggestion, container, false)
    }

    companion object {
        private const val TAG = "WifiSuggestionFragment"
    }
}