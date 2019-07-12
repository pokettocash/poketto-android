package com.poketto.poketto.controllers

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_payment_success.*
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService


class PaymentSuccessActivity : AppCompatActivity() {

    private val weiToDaiRate = 1000000000000000000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.poketto.poketto.R.layout.activity_payment_success)


        val receiptJson = intent.getStringExtra("receipt")
        val receipt = Gson().fromJson(receiptJson, TransactionReceipt::class.java)

        val endpoint = "https://dai.poa.network"
        val web3 = Web3j.build(HttpService(endpoint))

        val transaction = web3.ethGetTransactionByHash(receipt.transactionHash).sendAsync().get()

        address_text_view.text = receipt.to
        val formattedDaiString = String.format("%.2f", transaction.transaction.get().value.toFloat() / weiToDaiRate)
        value!!.text = formattedDaiString

        share_payment_button.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "https://blockscout.com/poa/dai/tx/${receipt.transactionHash}/internal_transactions")
                type = "text/plain"
            }
            startActivity(sendIntent)
        }

        val fromDetails = intent.getBooleanExtra("from_details", false)

        send_button.setOnClickListener {
            if(fromDetails) {
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }
}