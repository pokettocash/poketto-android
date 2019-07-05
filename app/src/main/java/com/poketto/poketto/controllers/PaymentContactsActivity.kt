package com.poketto.poketto.controllers

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView
import com.poketto.poketto.R
import kotlinx.android.synthetic.main.request_modal.*

class PaymentContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_contacts)

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

        val pasteButton = findViewById<LinearLayout>(R.id.clipboard_layout)
        val pasteAddressTextView = findViewById<TextView>(R.id.address_text_view)

        if(clipboard != null && clipboard.primaryClip != null) {
            if(clipboard.primaryClip!!.getItemAt(0).text.startsWith("0x")) {
                pasteAddressTextView.text = clipboard.primaryClip!!.getItemAt(0).text
            }
        }

        pasteButton.setOnClickListener {
            if(clipboard != null && clipboard.primaryClip != null) {
                if (clipboard.primaryClip!!.getItemAt(0).text.startsWith("0x")) {
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", clipboard.primaryClip!!.getItemAt(0).text)
                    startActivity(intent)
                }
            }
        }
    }
}