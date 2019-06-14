package com.poketto.poketto

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.security.SecureRandom
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.Web3j
import android.content.Context
import android.content.SharedPreferences
import android.widget.TextView
import de.adorsys.android.securestoragelibrary.SecurePreferences
import de.adorsys.android.securestoragelibrary.SecureStorageException
import org.web3j.crypto.*
import org.web3j.crypto.Hash.sha256
import java.math.BigInteger
import org.web3j.utils.Numeric
import org.web3j.crypto.TransactionEncoder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private val weiToDaiRate = 1000000000000000000
    val GAS_PRICE = BigInteger.valueOf(1_000_000_000L)
    val GAS_LIMIT = BigInteger.valueOf(21000L)
    private var balanceTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        balanceTextView = findViewById(R.id.balance)

        clearCredentials()

        var address = getAddress()

        if(address == null) {
            generate()
            address = getAddress()
        }
        Log.d("onCreate", "address: " + address)

        balanceFrom(address!!)
        transactionsFrom(address!!)
//        send("0x3849bA8A4D7193bF550a6e04632b176F9Ce1B7e8", "0.01")
    }

    private fun clearCredentials() {

        val editor: SharedPreferences.Editor
        val preferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val firstRun = preferences.getString("FirstRun", null)

        if(firstRun == null) {
            Log.d("clearCredentials", "on first run")
            editor = preferences.edit()
            editor.putString("FirstRun", "1strun")
            editor.apply()
            SecurePreferences.removeValue(this, "mnemonic")
        }
    }

    private fun getAddress() : String? {

        if(SecurePreferences.contains(this, "mnemonic")) {
            val mnemonic = SecurePreferences.getStringValue(this, "mnemonic", null)
            val seed = MnemonicUtils.generateSeed(mnemonic, "Poketto")
            val credentials = Credentials.create(ECKeyPair.create(sha256(seed)))
            val address = credentials.address
            Log.d("getAddress", "address: " + address)
            return address
        } else {
            return null
        }
    }

    private fun generate() {

        val initialEntropy = ByteArray(32)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(initialEntropy)

        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
        Log.d("generate", "mnemonic: " + mnemonic)

        try {
            SecurePreferences.setValue(this, "mnemonic", mnemonic)
        } catch (e: SecureStorageException) {
            handleException(e)
        }
    }

    private fun importSeed(seed: String) : Boolean {

        SecurePreferences.setValue(this, "mnemonic", seed)
        return false
    }

    private fun exportSeed() : String? {

        return SecurePreferences.getStringValue(this, "mnemonic", null)
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

        var dai = wei.toDouble() / weiToDaiRate

        balanceTextView!!.text = "Balance: " + dai.toString()

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

    private fun handleException(e: SecureStorageException) {
        Log.e("handleException", e.message)
    }

}
