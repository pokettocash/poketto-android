package com.poketto.poketto.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import java.util.ArrayList

class ContactsDAO(context: Context) {

    private var database: SQLiteDatabase? = null
    private var dbHelper: DbHelper? = null
    var context: Context? = null


    init {
        this.context = context
        dbHelper = DbHelper(context)
    }

    @Throws(SQLiteException::class)
    fun open() {

        database = dbHelper!!.writableDatabase
    }

    private fun close() {

        dbHelper!!.close()
    }

    fun save(contact: Contact?) {

        if (contact == null) {
            return
        }
        try {
            open()

            val select = arrayOf(ContactContract.Entry._ID)
            val cursor = database!!.query(
                ContactContract.Entry.TABLE_NAME,
                select,
                select[0] + " = ?",
                arrayOf("" + contact.contact_id),
                null,
                null,
                null
            )

            val exists = cursor.count > 0
            Log.d("ContactsDAO exists: ", exists.toString())

            cursor.close()

            val values = ContentValues()
            values.put(ContactContract.Entry._ID, contact.contact_id)
            values.put(ContactContract.Entry.ADDRESS, contact.address)
            values.put(ContactContract.Entry.AVATAR_URL, contact.avatar_url)
            values.put(ContactContract.Entry.CONTACT_ID, contact.contact_id)
            values.put(ContactContract.Entry.NAME, contact.name)

            if (exists) {
                database!!.update(ContactContract.Entry.TABLE_NAME, values, select[0] + " = ?", arrayOf("" + contact.contact_id))
            } else {
                database!!.insert(ContactContract.Entry.TABLE_NAME, null, values)
            }
        } catch (e: SQLiteException) {
            Log.d("ContactsDAO exception: ", e.localizedMessage)
        } finally {
            close()
        }
    }

    fun getContacts(): ArrayList<Contact> {

        var contact: Contact

        val result = arrayListOf<Contact>()

        val projection = arrayOf(
            ContactContract.Entry._ID,
            ContactContract.Entry.ADDRESS,
            ContactContract.Entry.AVATAR_URL,
            ContactContract.Entry.CONTACT_ID,
            ContactContract.Entry.NAME
        )

        try {
            open()

            val cursor = database!!.query(
                ContactContract.Entry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
            )

            Log.d("ContactsDAO cursor: ", cursor.toString())

            while (cursor.moveToNext()) {
                Log.d("ContactsDAO", "moveToNext")
                contact = Contact()
                contact.contact_id = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.CONTACT_ID))
                contact.address = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.ADDRESS))
                contact.avatar_url = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.AVATAR_URL))
                contact.name = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.NAME))
                result.add(contact)
            }
            cursor.close()
            close()
        } catch (e: SQLiteException) {
            Log.d("ContactsDAO exception: ", e.localizedMessage)
            e.printStackTrace()
        }

        return result
    }
}