package com.poketto.poketto.data

import android.provider.BaseColumns

class ContactContract {

    companion object {
        val createTable = "CREATE TABLE " + Entry.TABLE_NAME + " (" +
                Entry._ID + " TEXT," +
                Entry.ADDRESS + " TEXT," +
                Entry.AVATAR_URL + " TEXT," +
                Entry.CONTACT_ID + " TEXT, " +
                Entry.NAME + " TEXT)"
    }

    abstract class Entry : BaseColumns {
        companion object {

            var _ID = BaseColumns._ID
            val TABLE_NAME = "contact"
            val ADDRESS = "address"
            val AVATAR_URL = "avatar_url"
            val CONTACT_ID = "contact_id"
            val NAME = "name"
        }
    }  // Entry
}