package com.poketto.poketto.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.provider.ContactsContract
import android.content.ContentUris


class PhoneContactUtils(context: Context) {

    var context: Context? = null

    init {
        this.context = context
    }

    fun fetchContactIdFromPhoneNumber(phoneNumber: String): String {
        val uri = Uri.withAppendedPath(
            PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = this.context!!.contentResolver.query(
            uri,
            arrayOf(PhoneLookup.DISPLAY_NAME, PhoneLookup._ID), null, null, null
        )

        var contactId = ""

        if (cursor.moveToFirst()) {
            do {
                contactId = cursor.getString(
                    cursor
                        .getColumnIndex(PhoneLookup._ID)
                )
            } while (cursor.moveToNext())
        }

        return contactId
    }

     fun getPhotoUri(contactId:Long):Uri? {
        val contentResolver = this.context!!.contentResolver

        try {
            val cursor = contentResolver
            .query(ContactsContract.Data.CONTENT_URI, null,
                ContactsContract.Data.CONTACT_ID
                + "="
                + contactId
                + " AND "

                + ContactsContract.Data.MIMETYPE
                + "='"
                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                + "'", null, null)

            if (cursor != null) {
                if (!cursor!!.moveToFirst()) {
                    return null // no photo
                }
            }
            else {
                return null // error in cursor process
            }

            }
        catch (e:Exception) {
            e.printStackTrace()
            return null
        }

        val person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)

     }

}