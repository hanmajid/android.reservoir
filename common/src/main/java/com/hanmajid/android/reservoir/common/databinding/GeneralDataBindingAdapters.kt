package com.hanmajid.android.reservoir.common.databinding

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

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

@BindingAdapter(value = ["backgroundIf", "backgroundIfColor"])
fun setBackgroundIf(view: View?, condition: Boolean?, backgroundIfColor: Int) {
    view?.apply {
        setBackgroundColor(
            if (condition == true) backgroundIfColor
            else ContextCompat.getColor(context, android.R.color.transparent)
        )
    }
}

@BindingAdapter("dividerItemDecorationVertical")
fun dividerItemDecorationVertical(recyclerView: RecyclerView?, enabled: Boolean) {
    recyclerView?.apply {
        if (enabled) {
            val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            addItemDecoration(decoration)
        }
    }
}