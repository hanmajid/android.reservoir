package com.hanmajid.android.reservoir.common.databinding

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

@BindingAdapter("hidden")
fun setHidden(view: View?, isHidden: Boolean) {
    view?.isVisible = isHidden != true
}