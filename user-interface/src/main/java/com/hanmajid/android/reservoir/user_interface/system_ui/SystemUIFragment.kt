package com.hanmajid.android.reservoir.user_interface.system_ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hanmajid.android.reservoir.user_interface.databinding.FragmentSystemUiBinding

class SystemUIFragment : Fragment() {

    private lateinit var binding: FragmentSystemUiBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSystemUiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBinding()
    }

    private fun setupBinding() {
        binding.lifecycleOwner = viewLifecycleOwner

        val initialLayoutFlags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            } else {
                0
            }

        SystemUIUtil.setupSystemUI(requireActivity(), initialLayoutFlags)

        binding.buttonShowStatNavBar.setOnClickListener {
            SystemUIUtil.showStatusBar(requireActivity(), initialLayoutFlags)
        }

        binding.buttonDimStatBar.setOnClickListener {
            SystemUIUtil.dimStatusBar(requireActivity(), initialLayoutFlags)
        }

        binding.buttonHideStatBar.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SystemUIUtil.hideStatusBar(requireActivity(), initialLayoutFlags, false)
            } else {
                showSnackBar("This feature requires API >= Kitkat")
            }
        }

        binding.buttonHideNavBar.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SystemUIUtil.hideNavigationBar(
                    requireActivity(), initialLayoutFlags,
                    isImmersive = false,
                    isImmersiveSticky = false
                )
            } else {
                showSnackBar("This feature requires API >= Kitkat")
            }
        }

        binding.buttonLeanback.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                SystemUIUtil.fullscreenLeanback(requireActivity(), initialLayoutFlags)
            } else {
                showSnackBar("This feature requires API >= Jelly Bean")
            }
        }

        binding.buttonImmersive.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SystemUIUtil.fullscreenImmersive(requireActivity(), initialLayoutFlags)
            } else {
                showSnackBar("This feature requires API >= Kitkat")
            }
        }

        binding.buttonImmersiveSticky.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SystemUIUtil.fullscreenImmersiveSticky(requireActivity(), initialLayoutFlags)
            } else {
                showSnackBar("This feature requires API >= Kitkat")
            }
        }

        requireActivity().window?.decorView?.setOnSystemUiVisibilityChangeListener { visibility ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                binding.fullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            } else {
                showSnackBar("This feature requires API >= Jelly Bean")
            }
            binding.lowProfile = visibility and View.SYSTEM_UI_FLAG_LOW_PROFILE != 0
            binding.hideNavigation = visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "SystemUIFragment"
    }
}