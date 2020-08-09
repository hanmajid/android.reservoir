package com.hanmajid.android.reservoir.common.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hanmajid.android.reservoir.common.R

class PermissionUtil {

    companion object {
        @JvmStatic
        fun allPermissionsGranted(
            context: Context,
            permissions: Array<String>
        ): Boolean {
            return permissions.all {
                ContextCompat.checkSelfPermission(
                    context, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        @JvmStatic
        fun requestPermissionIfNeeded(
            fragment: Fragment,
            permission: String,
            rationale: String?,
            onResult: (Boolean?) -> Unit
        ) {
            if (allPermissionsGranted(fragment.requireContext(), arrayOf(permission))) {
                onResult(true)
            } else {
                val provideRationale = fragment.shouldShowRequestPermissionRationale(permission)

                if (provideRationale && rationale != null) {
                    Snackbar.make(
                        fragment.requireView(),
                        rationale,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(fragment.requireContext().getString(R.string.allow)) {
                            // Request permission
                            requestPermission(
                                fragment,
                                permission,
                                onResult
                            )
                        }
                        .show()
                } else {
                    // Request permission
                    requestPermission(
                        fragment,
                        permission,
                        onResult
                    )
                }
            }
        }

        @JvmStatic
        fun requestPermissionsIfNeeded(
            fragment: Fragment,
            permissions: Array<String>,
            rationale: String?,
            onResult: (Map<String, Boolean>?) -> Unit
        ) {
            if (allPermissionsGranted(fragment.requireContext(), permissions)) {
                onResult(mapOf("" to true))
            } else {
                val provideRationale = permissions.any {
                    fragment.shouldShowRequestPermissionRationale(it)
                }
                if (provideRationale && rationale != null) {
                    Snackbar.make(
                        fragment.requireView(),
                        rationale,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(fragment.requireContext().getString(R.string.allow)) {
                            // Request permission
                            requestPermissions(
                                fragment,
                                permissions,
                                onResult
                            )
                        }
                        .show()
                } else {
                    // Request permission
                    requestPermissions(
                        fragment,
                        permissions,
                        onResult
                    )
                }
            }
        }

        @JvmStatic
        fun requestPermission(
            fragment: Fragment,
            permission: String,
            onResult: (Boolean) -> Unit
        ) {
            val contract =
                fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    onResult(it)
                }
            contract.launch(permission)
        }

        @JvmStatic
        fun requestPermissions(
            fragment: Fragment,
            permissions: Array<String>,
            onResult: (Map<String, Boolean>) -> Unit
        ) {
            val contract =
                fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    onResult(it)
                }
            contract.launch(permissions)
        }
    }
}