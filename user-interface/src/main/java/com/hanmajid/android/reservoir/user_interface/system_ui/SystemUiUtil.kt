package com.hanmajid.android.reservoir.user_interface.system_ui

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi

object SystemUIUtil {

    fun setupSystemUI(activity: Activity, initialLayoutFlags: Int) {
        activity.window?.decorView?.apply {
            systemUiVisibility = initialLayoutFlags
        }
    }

    fun showStatusBar(activity: Activity, initialLayoutFlags: Int = 0) {
        activity.window?.decorView?.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE or initialLayoutFlags
        }
    }

    fun dimStatusBar(activity: Activity, initialLayoutFlags: Int = 0) {
        activity.window?.decorView?.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or initialLayoutFlags
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun hideStatusBar(
        activity: Activity,
        initialLayoutFlags: Int = 0,
        isImmersiveSticky: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            val immersiveStickyFlag =
                if (isImmersiveSticky) View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY else 0
            activity.window?.decorView?.apply {
                systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or initialLayoutFlags
                        or immersiveStickyFlag)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun hideNavigationBar(
        activity: Activity,
        initialLayoutFlags: Int = 0,
        isImmersive: Boolean = false,
        isImmersiveSticky: Boolean = false
    ) {
        val immersiveFlag = if (isImmersive) View.SYSTEM_UI_FLAG_IMMERSIVE else 0
        val immersiveStickyFlag =
            if (isImmersiveSticky) View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY else 0
        // Prioritized immersive over immersive sticky.
        val usedImmersiveFlag = if (isImmersive) immersiveFlag else immersiveStickyFlag
        activity.window?.decorView?.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or initialLayoutFlags
                    or usedImmersiveFlag)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    const val FULLSCREEN_LAYOUT_FLAGS = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun fullscreenLeanback(
        activity: Activity,
        initialLayoutFlags: Int = FULLSCREEN_LAYOUT_FLAGS
    ) {
        activity.window?.decorView?.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or initialLayoutFlags)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun fullscreenImmersive(
        activity: Activity,
        initialLayoutFlags: Int = FULLSCREEN_LAYOUT_FLAGS
    ) {
        activity.window?.decorView?.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or initialLayoutFlags)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun fullscreenImmersiveSticky(
        activity: Activity,
        initialLayoutFlags: Int = FULLSCREEN_LAYOUT_FLAGS
    ) {
        activity.window?.decorView?.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or initialLayoutFlags)
        }
    }
}