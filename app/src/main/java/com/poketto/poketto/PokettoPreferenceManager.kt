package com.poketto.poketto

import android.content.Context
import android.content.SharedPreferences


class PokettoPreferenceManager(context: Context) {

    var sharedPreferences: SharedPreferences? = null
    var spEditor: SharedPreferences.Editor? = null
    var context: Context? = null

    private val FIRST_LAUNCH = "firstLaunch"
    var MODE = 0
    private val PREFERENCE = "Javapapers"

    init {
        this.context = context
        sharedPreferences = context.getSharedPreferences(PREFERENCE, MODE)
        spEditor = sharedPreferences!!.edit()
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        spEditor!!.putBoolean(FIRST_LAUNCH, isFirstTime)
        spEditor!!.commit()
    }

    fun firstLaunch(): Boolean {
        return sharedPreferences!!.getBoolean(FIRST_LAUNCH, true)
    }
}