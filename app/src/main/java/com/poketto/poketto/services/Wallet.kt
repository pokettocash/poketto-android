package com.poketto.poketto.services

import android.content.Context
import android.util.Log
import de.adorsys.android.securestoragelibrary.SecurePreferences
import de.adorsys.android.securestoragelibrary.SecureStorageException
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.SecureRandom

class Wallet(context: Context) {

    val GAS_PRICE = BigInteger.valueOf(1_000_000_000L)
    val GAS_LIMIT = BigInteger.valueOf(21000L)

    private val weiToDaiRate = 1000000000000000000

    private var context: Context? = context

    fun getAddress() : String? {

        if(SecurePreferences.contains(context!!, "mnemonic")) {

            val mnemonic = SecurePreferences.getStringValue(context!!, "mnemonic", null)

            val seed = MnemonicUtils.generateSeed(mnemonic, "")
            val credentials = Credentials.create(ECKeyPair.create(Hash.sha256(seed)))
            val address = credentials.address
            Log.d("getAddress", "address: $address")
            return address
        }

        return null
    }

    fun generate() {

        val initialEntropy = ByteArray(32)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(initialEntropy)

        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
        Log.d("generate", "mnemonic: $mnemonic")

        try {
            SecurePreferences.setValue(context!!, "mnemonic", mnemonic)
        } catch (e: SecureStorageException) {
            handleException(e)
        }
    }

    fun importSeed(seed: String) : Boolean {

        SecurePreferences.setValue(context!!, "mnemonic", seed)
        return false
    }

    fun exportSeed() : String? {

        return SecurePreferences.getStringValue(context!!, "mnemonic", null)
    }

    fun balanceFrom(address: String) : Double {

        // connect to node
        val web3 = Web3j.build(HttpService("https://dai.poa.network"))

        // send asynchronous requests to get balance
        val ethGetBalance = web3
            .ethGetBalance(address, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()

        val wei = ethGetBalance.balance

        val dai = wei.toDouble() / weiToDaiRate

        return dai
    }

    private fun handleException(e: SecureStorageException) {
        Log.e("handleException", e.message)
    }

    fun send(toAddress: String, value: String) {

        val endpoint = "https://dai.poa.network"
        val web3 = Web3j.build(HttpService(endpoint))
        val mnemonic = SecurePreferences.getStringValue(context!!, "mnemonic", null)
        val seed = MnemonicUtils.generateSeed(mnemonic, "")
        val credentials = Credentials.create(ECKeyPair.create(Hash.sha256(seed)))

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