package com.poketto.poketto.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_VERSION = 1
const val DATABASE_NAME = "contacts.db"

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME,
        null, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(ContactContract.createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int,
                           newVersion: Int) {

    }
}