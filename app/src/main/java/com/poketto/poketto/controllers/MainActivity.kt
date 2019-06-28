package com.poketto.poketto.controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.Web3j
import android.widget.TextView
import com.poketto.poketto.api.RetrofitInitializer
import com.poketto.poketto.models.Transactions
import com.poketto.poketto.services.Wallet
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.web3j.crypto.*
import org.web3j.crypto.Hash.sha256
import java.math.BigInteger
import org.web3j.utils.Numeric
import org.web3j.crypto.TransactionEncoder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.Menu
import android.text.Spannable
import android.text.style.ImageSpan
import android.text.SpannableString
import android.view.View
import android.widget.Button
import android.app.Dialog
import android.view.MenuItem
import android.widget.ImageView
import net.glxn.qrgen.android.QRCode
import com.poketto.poketto.R


class MainActivity : AppCompatActivity() {

    private val weiToDaiRate = 1000000000000000000
    val GAS_PRICE = BigInteger.valueOf(1_000_000_000L)
    val GAS_LIMIT = BigInteger.valueOf(21000L)
    private var balanceTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requestButton = findViewById<View>(R.id.request_btn) as Button
        val requestButtonLabel = SpannableString("   Request")
        requestButtonLabel.setSpan(
            ImageSpan(
                applicationContext, R.drawable.arrow_down,
                ImageSpan.ALIGN_BOTTOM
            ), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        requestButton.text = requestButtonLabel
        requestButton.setOnClickListener {
            showRequestModal()
        }

        val payButton = findViewById<View>(R.id.pay_btn) as Button
        val payButtonLabel = SpannableString("   Pay")
        payButtonLabel.setSpan(
            ImageSpan(
                applicationContext, R.drawable.arrow_up,
                ImageSpan.ALIGN_BOTTOM
            ), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        payButton.text = payButtonLabel

        //setting toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.elevation = 0F
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        balanceTextView = findViewById(R.id.balance_value)

        val address = Wallet(this).getAddress()

        Log.d("onCreate", "address: " + address)

        balanceFrom(address!!)
        transactionsFrom(address!!)
//        send("0x3849bA8A4D7193bF550a6e04632b176F9Ce1B7e8", "0.01")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.nav_button_qrcode -> {
                showRequestModal()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showRequestModal() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.request_modal)
        dialog.setTitle("")

        val address = Wallet(this).getAddress()
        val addressTextView = dialog.findViewById(R.id.address) as TextView
        addressTextView.text = address
        val image = dialog.findViewById(R.id.image) as ImageView

        val bitmap = QRCode.from(address).withSize(3000, 3000).bitmap()
        image.setImageBitmap(bitmap)

//        val dialogButton = dialog.findViewById(R.id.dialogButtonOK) as Button
//        dialogButton.setOnClickListener {
//            dialog.dismiss()
//        }

        dialog.show()
    }


    private fun balanceFrom(address: String) {

        // connect to node
        val web3 = Web3j.build(HttpService("https://dai.poa.network"))

        // send asynchronous requests to get balance
        val ethGetBalance = web3
            .ethGetBalance(address, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()

        val wei = ethGetBalance.balance

        val dai = wei.toDouble() / weiToDaiRate

        val formattedDaiString = String.format("%.2f", dai)

        balanceTextView!!.text = "$formattedDaiString xDai"

        Log.d("balanceFrom", "dai balance: " + dai)
    }

    private fun transactionsFrom(address: String) {

        val call = RetrofitInitializer().explorer().transactions("account", "txlist", address)
        call.enqueue(object: Callback<Transactions?> {
                override fun onResponse(call: Call<Transactions?>?,
                                        response: Response<Transactions?>?) {
                    response?.body()?.let {
                        val transactions: Transactions = it
                        Log.d("onResponse", "transactions: " + transactions.result)
                    }
                }

                override fun onFailure(call: Call<Transactions?>?,
                                       t: Throwable?) {
                    Log.d("onFailure", "transactions: " + t?.localizedMessage)
                }
            }
        )
    }

    private fun send(toAddress: String, value: String) {

        val endpoint = "https://dai.poa.network"
        val web3 = Web3j.build(HttpService(endpoint))
        val mnemonic = SecurePreferences.getStringValue(this, "mnemonic", null)
        val seed = MnemonicUtils.generateSeed(mnemonic, "Poketto")
        val credentials = Credentials.create(ECKeyPair.create(sha256(seed)))

        val ethGetTransactionCount = web3.ethGetTransactionCount(
            credentials.address, DefaultBlockParameterName.LATEST
        ).sendAsync().get()

        val nonce = ethGetTransactionCount.transactionCount

        val rawTransaction = RawTransaction.createEtherTransaction(
                nonce, GAS_PRICE, GAS_LIMIT, toAddress, BigInteger.valueOf((value.toFloat()*weiToDaiRate).toLong()))

        Log.d("send", "rawTransaction: " + rawTransaction)

        val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
        val hexValue = Numeric.toHexString(signedMessage)

        val ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get()
        val transactionReceipt = ethSendTransaction.transactionHash

//        val transactionReceipt = Transfer.sendFunds(
//            web3, credentials, "0x4b2Cbe48C378CaD5A2145358440B25627F99E189",
//            BigDecimal.valueOf(0.01), Convert.Unit.GWEI
//        )
        Log.d("send", "ethSendTransaction: " + ethSendTransaction)
        Log.d("send", "send tx receipt: " + transactionReceipt)
    }

}
