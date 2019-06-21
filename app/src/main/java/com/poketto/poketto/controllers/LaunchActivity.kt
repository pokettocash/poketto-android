package com.poketto.poketto.controllers

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.poketto.poketto.R
import com.poketto.poketto.services.Wallet
import de.adorsys.android.securestoragelibrary.SecurePreferences


class LaunchActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        clearCredentials()

        val address = Wallet(this).getAddress()

        if(address == null) {
            val intent = Intent(applicationContext, OnboardingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun clearCredentials() {

        val editor: SharedPreferences.Editor
        val preferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val firstRun = preferences.getString("FirstRun", null)

        if(firstRun == null) {
            Log.d("clearCredentials", "on first run")
            editor = preferences.edit()
            editor.putString("FirstRun", "1strun")
            editor.apply()
            SecurePreferences.removeValue(this, "mnemonic")
        }
    }

}