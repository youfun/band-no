package dev.bandno.app.screening

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class ContactDirectory(
    private val context: Context,
) {
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    fun isContact(number: String): Boolean {
        if (number.isBlank() || !hasPermission()) return false
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number),
        )
        return context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null,
        )?.use { cursor -> cursor.moveToFirst() } ?: false
    }
}
