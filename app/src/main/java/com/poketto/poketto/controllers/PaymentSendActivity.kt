package com.poketto.poketto.controllers

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.poketto.poketto.services.Wallet
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import com.google.gson.Gson


class PaymentSendActivity : AppCompatActivity() {

    private var amountEditText: EditText? = null
    private var mAddress: String = ""
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.poketto.poketto.R.layout.activity_payment_send)

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(com.poketto.poketto.R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = "Send Payment"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        amountEditText = findViewById(com.poketto.poketto.R.id.amount_edit_text)

        mAddress = intent.getStringExtra("address")
        val addressTextView = findViewById<TextView>(com.poketto.poketto.R.id.address_text_view)
        addressTextView.text = mAddress

        val sendButton = findViewById<Button>(com.poketto.poketto.R.id.send_button)
        sendButton!!.setOnClickListener {
            sendTransaction()
        }

        val maxButton = findViewById<Button>(com.poketto.poketto.R.id.send_max_button)
        maxButton!!.setOnClickListener {
            val transactionCost = 0.000021F
            val address = Wallet(this).getAddress()
            val balance = Wallet(this).balanceFrom(address!!)
            val maxAmount = String.format("%f", balance-transactionCost)
            amountEditText!!.setText(maxAmount)
            maxButton.setTextColor(Color.parseColor("#216BFE"))
        }

    }

    private fun showLoadingSpinner() {

        val promptsView = View.inflate(this, com.poketto.poketto.R.layout.dialog_progress, null)
        val progressBar = promptsView.findViewById(com.poketto.poketto.R.id.dialog_progress_bar) as ProgressBar
        progressBar.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(this, com.poketto.poketto.R.color.colorAccent),
            android.graphics.PorterDuff.Mode.MULTIPLY
        )

        val alertDialogBuilder = AlertDialog.Builder(this, com.poketto.poketto.R.style.Theme_AppCompat_Light_Dialog)
        alertDialogBuilder.setView(promptsView)
        alertDialog = alertDialogBuilder.create()
        alertDialog!!.show()
        alertDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun hideLoadingSpinner() {

        alertDialog!!.dismiss()
    }

    private fun sendTransaction() {

        showLoadingSpinner()

        doAsync {

            Wallet(this@PaymentSendActivity).send(mAddress, amountEditText!!.text.toString(), success = {

                val transactionReceipt = it

                uiThread {

                    hideLoadingSpinner()
                    val intent = Intent(this@PaymentSendActivity, PaymentSuccessActivity::class.java)
                    intent.putExtra("receipt", Gson().toJson(transactionReceipt))
                    intent.putExtra("from_details", false)
                    startActivity(intent)
                }

            }, failure = {

                val message = it

                uiThread {

                    hideLoadingSpinner()
                    Toast.makeText(this@PaymentSendActivity, message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

}