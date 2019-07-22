package com.poketto.poketto.controllers

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_payment_contacts.*
import android.Manifest.permission
import android.app.Activity
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import org.jetbrains.anko.startActivity

const val CAMERA_PERMISSION = 1001

class PaymentContactsActivity : AppCompatActivity() {

    val QRCODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.poketto.poketto.R.layout.activity_payment_contacts)

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(com.poketto.poketto.R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

        val pasteAddressTextView = findViewById<TextView>(com.poketto.poketto.R.id.address_text_view)

        if(clipboard != null && clipboard.primaryClip != null) {
            if(clipboard.primaryClip!!.getItemAt(0).text.startsWith("0x")) {
                pasteAddressTextView.text = clipboard.primaryClip!!.getItemAt(0).text
            } else if (clipboard.primaryClip!!.getItemAt(0).text.startsWith("ethereum:")) {
                pasteAddressTextView.text = clipboard.primaryClip!!.getItemAt(0).text.substring(9)
            }
        }

        clipboard_layout.setOnClickListener {
            if(clipboard != null && clipboard.primaryClip != null) {
                if (clipboard.primaryClip!!.getItemAt(0).text.startsWith("0x")) {
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", clipboard.primaryClip!!.getItemAt(0).text)
                    startActivity(intent)
                } else if (clipboard.primaryClip!!.getItemAt(0).text.startsWith("ethereum:")) {
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", clipboard.primaryClip!!.getItemAt(0).text.substring(9))
                    startActivity(intent)
                }
            }
        }

        scan_layout.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), CAMERA_PERMISSION)
            } else {
                val intent = Intent(this, CustomQRCodeActivity::class.java)
                startActivityForResult(intent, QRCODE)
            }
        }
    }

    override
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, CustomQRCodeActivity::class.java)
                    startActivityForResult(intent, QRCODE)
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == QRCODE) {

            if(resultCode == RESULT_OK) {
                val scannedResult = data!!.getStringExtra("address")

                if(scannedResult.startsWith("0x")) {
                    Log.d("address", "address: " + scannedResult)
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", scannedResult)
                    startActivity(intent)
                } else if(scannedResult.startsWith("ethereum:")) {
                    val address = scannedResult.substring(9)
                    Log.d("address", "address: " + address)
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", address)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@PaymentContactsActivity, "Invalid address", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}