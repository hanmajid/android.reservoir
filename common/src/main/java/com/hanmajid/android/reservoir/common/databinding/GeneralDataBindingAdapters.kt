package com.hanmajid.android.reservoir.common.databinding

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

@BindingAdapter("hidden")
fun setHidden(view: View?, isHidden: Boolean) {
    view?.isVisible = isHidden != true
}

@BindingAdapter(value = ["iconIf", "iconIfTrue", "iconIfFalse"])
fun setIconIf(
    imageView: ImageView?,
    iconIf: Boolean?,
    iconIfTrue: Drawable?,
    iconIfFalse: Drawable?
) {
    imageView?.apply {
        if (iconIfTrue != null && iconIfFalse != null) {
            setImageDrawable(
                if (iconIf == true) iconIfTrue else iconIfFalse
            )
        }
    }
}