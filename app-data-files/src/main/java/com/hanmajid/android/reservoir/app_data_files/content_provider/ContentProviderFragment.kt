package com.hanmajid.android.reservoir.app_data_files.content_provider

import android.content.ContentUris
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.UserDictionary
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.hanmajid.android.reservoir.app_data_files.R
import com.hanmajid.android.reservoir.app_data_files.content_provider.contacts.ContactListAdapter
import com.hanmajid.android.reservoir.app_data_files.content_provider.contacts.MyContact
import com.hanmajid.android.reservoir.app_data_files.databinding.FragmentContentProviderBinding
import com.hanmajid.android.reservoir.common.util.PermissionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.jar.Manifest

class ContentProviderFragment : Fragment() {

    private lateinit var binding: FragmentContentProviderBinding

    private val adapter = ContactListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContentProviderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBinding()

        PermissionUtil.requestPermissionIfNeeded(
            this,
            android.Manifest.permission.READ_CONTACTS,
            null
        ) {
            if (it == true) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val projections = arrayOf(
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts.STARRED,
                        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                    )
                    val selectionClause: String? = null
                    val selectionArgs: Array<String>? = null
                    val sortOrder: String? = null
                    val cursor = requireActivity().contentResolver.query(
//                        ContactsContract.Contacts.CONTENT_URI,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projections,
                        selectionClause,
                        selectionArgs,
                        sortOrder
                    )

                    val contacts = mutableListOf<MyContact>()
                    when (cursor?.count) {
                        null -> {
                            Log.wtf(TAG, "Error cursor count null!")
                        }
                        0 -> {
                            Log.wtf(TAG, "Cursor count empty!")
                        }
                        else -> {
                            Log.wtf(TAG, "Cursor count: ${cursor.count}!")
                            cursor.apply {
                                val indexId = getColumnIndex(ContactsContract.Contacts._ID)
                                val indexDisplayName =
                                    getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                                val indexPhotoUri =
                                    getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                                val indexPhotoThumbnailUri =
                                    getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
                                val indexHasPhoneNumber =
                                    getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                                val indexStarred = getColumnIndex(ContactsContract.Contacts.STARRED)
                                val indexLastUpdated =
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                        getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
                                    } else {
                                        null
                                    }
                                val indexPhoneNumber =
                                    getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                val indexPhoneType =
                                    getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                                while (moveToNext()) {
                                    contacts.add(
                                        MyContact(
                                            id = getLong(indexId),
                                            displayName = getString(indexDisplayName),
                                            photoUri = getInt(indexPhotoUri),
                                            photoThumbnailUri = getInt(indexPhotoThumbnailUri),
                                            hasPhoneNumber = getInt(indexHasPhoneNumber) == 1,
                                            starred = getInt(indexStarred) == 1,
                                            lastUpdatedTimestamp = indexLastUpdated?.let {
                                                Date(getLong(indexLastUpdated))
                                            },
                                            phoneNumber = getString(indexPhoneNumber),
                                            phoneType = getInt(indexPhoneType)
//                                        null,
//                                            null
                                        )
                                    )
                                }
                                Log.wtf(TAG, contacts.toString())
                                withContext(Dispatchers.Main) {
                                    adapter.submitList(contacts)
                                }
                            }
                        }
                    }
                    cursor?.close()
                }
            } else {
                Snackbar.make(view, "Permission not granted!", Snackbar.LENGTH_SHORT).show()
            }
        }

//        val id = 4L
//        ContentUris.withAppendedId(UserDictionary.Words.CONTENT_URI, id)
    }

    private fun setupBinding() {
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.adapter = adapter
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "ContentProviderFragment"
    }
}