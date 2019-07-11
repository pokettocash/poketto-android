package com.poketto.poketto.controllers

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.poketto.poketto.R
import com.poketto.poketto.services.Wallet
import kotlinx.android.synthetic.main.request_modal.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PaymentSendActivity : AppCompatActivity() {

    private var amountEditText: EditText? = null
    private var mAddress: String = ""
    private var alertDialog: AlertDialog? = null


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
            val transactionCost = 0.000021F
            val address = Wallet(this).getAddress()
            val balance = Wallet(this).balanceFrom(address!!)
            val maxAmount = String.format("%f", balance-transactionCost)
            amountEditText!!.setText(maxAmount)
            maxButton.setTextColor(Color.parseColor("#216BFE"))
        }

    }

    private fun showLoadingSpinner() {

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(amountEditText!!.getWindowToken(), 0)

        val promptsView = View.inflate(this, R.layout.dialog_progress, null)
        val progressBar = promptsView.findViewById(R.id.dialog_progress_bar) as ProgressBar
        progressBar.getIndeterminateDrawable().setColorFilter(
            ContextCompat.getColor(this, R.color.colorAccent),
            android.graphics.PorterDuff.Mode.MULTIPLY
        )

        val alertDialogBuilder = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog)
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
                    intent.putExtra("receipt", transactionReceipt.transactionHash)
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