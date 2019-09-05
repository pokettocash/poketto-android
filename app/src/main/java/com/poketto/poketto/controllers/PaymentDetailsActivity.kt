package com.poketto.poketto.controllers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.poketto.poketto.R
import com.poketto.poketto.models.Transaction
import kotlinx.android.synthetic.main.activity_payment_details.*
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.text.SimpleDateFormat
import java.util.*

const val CONTACTS_PERMISSION = 1002

class PaymentDetailsActivity: AppCompatActivity()  {

    private val weiToDaiRate = 1000000000000000000
    private var ownerAddress = ""
    private var otherAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_details)

        val transactionJson = intent.getStringExtra("TRANSACTION")
        val transaction = Gson().fromJson(transactionJson, Transaction::class.java)
        ownerAddress = intent.getStringExtra("ownerAddress")

        if(transaction.from == ownerAddress) {
            otherAddress =  transaction.to!!
        } else {
            otherAddress = transaction.from!!
        }

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = "Payment details"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        share_payment_button.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "https://blockscout.com/poa/dai/tx/${transaction.hash}/internal_transactions")
                type = "text/plain"
            }
            startActivity(sendIntent)
        }

        send_new_payment_button.setOnClickListener {
            val paymentSendIntent = Intent(this, PaymentSendActivity::class.java)
            paymentSendIntent.putExtra("address", otherAddress)
            startActivity(paymentSendIntent)
        }

        assign_address_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION)
            } else {
                assignAddressToContact()
            }
        }

        val formattedDaiString = String.format("%.2f", transaction.value!!.toFloat() / weiToDaiRate)
        value!!.text = formattedDaiString

        if(transaction.from == ownerAddress) {
            address_text_view.text = transaction.to
            recipient_text_view.text = this.resources.getString(R.string.to)
            sent_text_view.text = this.resources.getString(R.string.sent)
        } else {
            address_text_view.text = transaction.from
            recipient_text_view.text = this.resources.getString(R.string.from)
            sent_text_view.text = this.resources.getString(R.string.received)
        }

        date_text_view.text = getDateTime(transaction.timeStamp!!)
        hours_text_view.text = getHourTime(transaction.timeStamp)

        if(transaction.displayName != null) {
            assign_address_button.text = this.resources.getString(R.string.reassign_address)
            name_text_view.text = transaction.displayName
            name_text_view.visibility = View.VISIBLE
            address_text_view.text = String.format("%s...", address_text_view.text.take(10))
        }

        if(transaction.displayImage != null) {
            receiver_image.setImageURI(Uri.parse(transaction.displayImage))
        } else {
            receiver_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pay_unknown))
        }

    }

    override
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CONTACTS_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    assignAddressToContact()
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
        }
    }


    private fun assignAddressToContact() {

        val contactsIntent = Intent(this, ContactsActivity::class.java)
        contactsIntent.putExtra("address", otherAddress)
        startActivity(contactsIntent)
    }

    private fun getDateTime(s: String): String? {
        val sdfDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val netDate = Date(s.toLong()*1000)
        return sdfDate.format(netDate)
    }

    private fun getHourTime(s: String): String? {
        val sdfDate = SimpleDateFormat("hh:mm", Locale.getDefault())
        val netDate = Date(s.toLong()*1000)
        return sdfDate.format(netDate)
    }
}