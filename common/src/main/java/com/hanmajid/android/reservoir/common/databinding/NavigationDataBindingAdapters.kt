package com.hanmajid.android.reservoir.common.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.navigation.NavController
import androidx.navigation.NavDirections

@BindingAdapter("navController", "onClickNavDirections")
fun onClickNavigate(
    view: View?,
    navController: NavController?,
    navDirections: NavDirections?
) {
    view?.apply {
        if (navController != null && navDirections != null) {
            setOnClickListener {
                navController.navigate(navDirections)
            }
        }
    }
}