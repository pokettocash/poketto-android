package com.poketto.poketto.controllers

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.poketto.poketto.R
import com.poketto.poketto.services.Wallet
import kotlinx.android.synthetic.main.request_modal.*

class PaymentSendActivity : AppCompatActivity() {

    private var amountEditText: EditText? = null
    private var mAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_send)

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = "Send Payment"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        amountEditText = findViewById(R.id.amount_edit_text)

        mAddress = intent.getStringExtra("address")
        val addressTextView = findViewById<TextView>(R.id.address_text_view)
        addressTextView.text = mAddress

        val sendButton = findViewById<Button>(R.id.send_button)
        sendButton!!.setOnClickListener {
            sendTransaction()
        }

        val maxButton = findViewById<Button>(R.id.send_max_button)
        maxButton!!.setOnClickListener {
            Log.d("maxButton", "clicked")

            val transactionCost = 0.000021F
            val address = Wallet(this).getAddress()

            val balance = Wallet(this).balanceFrom(address!!)
            Log.d("balance", "balance " + balance)

            val maxAmount = String.format("%f", balance-transactionCost)
            amountEditText!!.setText(maxAmount)
            maxButton.setTextColor(Color.parseColor("#216BFE"))
        }

    }

    private fun sendTransaction() {

        Wallet(this).send(mAddress, amountEditText!!.text.toString())
    }
}