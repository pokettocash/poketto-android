package com.poketto.poketto.controllers

import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import com.poketto.poketto.R
import com.poketto.poketto.data.Contact
import com.poketto.poketto.models.Transaction
import kotlinx.android.synthetic.main.activity_payment_details.*
import kotlinx.android.synthetic.main.activity_payment_details.address_text_view
import kotlinx.android.synthetic.main.activity_payment_details.name_text_view
import kotlinx.android.synthetic.main.activity_payment_details.receiver_image
import kotlinx.android.synthetic.main.activity_payment_send.*


class PaymentSendActivity : AppCompatActivity() {

    private var amountEditText: EditText? = null
    private var mAddress: String = ""
    private var mContact: Contact? = null
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_send)

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = "Send Payment"
        val address = Wallet(this).getAddress()
        val balance = Wallet(this).balanceFrom(address!!)
        val formattedDaiString = String.format("%.2f", balance)
        supportActionBar!!.subtitle = "Balance $formattedDaiString xDai"

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        amountEditText = findViewById(R.id.amount_edit_text)

        mAddress = intent.getStringExtra("address")
        val contactJson = intent.getStringExtra("contact")
        mContact = Gson().fromJson(contactJson, Contact::class.java)

        address_text_view_unknown.text = mAddress
        address_text_view.text = mAddress

        val sendButton = findViewById<Button>(R.id.send_button)
        sendButton!!.setOnClickListener {
            sendTransaction()
        }

        val maxButton = findViewById<Button>(R.id.send_max_button)
        maxButton!!.setOnClickListener {
            val transactionCost = 0.000021F
            val maxAmount = String.format("%f", balance-transactionCost)
            amountEditText!!.setText(maxAmount)
            maxButton.setTextColor(Color.parseColor("#216BFE"))
        }

        if(mContact != null) {
            contact_layout.visibility = View.VISIBLE
            unknown_contact_layout.visibility = View.GONE
            name_text_view.text = mContact!!.name
            name_text_view.visibility = View.VISIBLE
        } else {
            contact_layout.visibility = View.GONE
            unknown_contact_layout.visibility = View.VISIBLE
        }

        if(mContact != null) {
            receiver_image.setImageURI(Uri.parse(mContact!!.avatar_url))
        } else {
            receiver_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pay_unknown))
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