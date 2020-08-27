package com.hanmajid.android.reservoir.app_data_files.content_provider.contacts

import java.util.*

data class MyContact(
    val id: Long,
    val displayName: String?,
    val photoUri: Int,
    val photoThumbnailUri: Int,
    val hasPhoneNumber: Boolean,
    val starred: Boolean,
    val lastUpdatedTimestamp: Date?,
    val phoneNumber: String?,
    val phoneType: Int?
)