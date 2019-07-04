package com.poketto.poketto.controllers

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

import com.poketto.poketto.R
import com.poketto.poketto.services.Wallet

class SeedActivity : AppCompatActivity() {

    private var seedEditText: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seed)

        seedEditText = findViewById(R.id.seed_edit_text)

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = "Import Seed"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.import_seed_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.nav_button_done -> {
                importSeed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun importSeed() {
        Wallet(this).importSeed(seedEditText!!.text.toString())
        finish()
    }
}