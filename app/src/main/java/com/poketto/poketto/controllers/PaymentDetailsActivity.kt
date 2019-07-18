package com.poketto.poketto.controllers

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import com.poketto.poketto.models.Transaction
import kotlinx.android.synthetic.main.activity_payment_details.*
import java.text.SimpleDateFormat
import java.util.*


class PaymentDetailsActivity: AppCompatActivity()  {

    private val weiToDaiRate = 1000000000000000000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.poketto.poketto.R.layout.activity_payment_details)

        val transactionJson = intent.getStringExtra("TRANSACTION")
        val transaction = Gson().fromJson(transactionJson, Transaction::class.java)
        val ownerAddress = intent.getStringExtra("ownerAddress")

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(com.poketto.poketto.R.id.toolbar)
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
            paymentSendIntent.putExtra("address", ownerAddress)
            startActivity(paymentSendIntent)
        }

        val formattedDaiString = String.format("%.2f", transaction.value!!.toFloat() / weiToDaiRate)
        value!!.text = formattedDaiString

        if(transaction.from == ownerAddress) {
            address_text_view.text = transaction.to
            recipient_text_view.text = this.resources.getString(com.poketto.poketto.R.string.to)
            sent_text_view.text = this.resources.getString(com.poketto.poketto.R.string.sent)
        } else {
            address_text_view.text = transaction.from
            recipient_text_view.text = this.resources.getString(com.poketto.poketto.R.string.from)
            sent_text_view.text = this.resources.getString(com.poketto.poketto.R.string.received)
        }

        date_text_view.text = getDateTime(transaction.timeStamp!!)
        hours_text_view.text = getHourTime(transaction.timeStamp)
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