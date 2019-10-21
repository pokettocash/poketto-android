package com.poketto.poketto.services

import android.content.Context
import android.util.Log
import de.adorsys.android.securestoragelibrary.SecurePreferences
import de.adorsys.android.securestoragelibrary.SecureStorageException
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.security.SecureRandom
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT
import org.web3j.crypto.MnemonicUtils
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Convert
import java.math.BigDecimal
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import org.web3j.utils.Numeric
import java.net.URLDecoder
import android.R.string
import java.nio.ByteBuffer
import java.nio.charset.Charset


class Wallet(context: Context) {

    val GAS_PRICE = BigInteger.valueOf(1_000_000_000L)
    val GAS_LIMIT = BigInteger.valueOf(21000L)
    private val weiToDaiRate = 1000000000000000000
    private var context: Context? = context

    fun getAddress() : String? {

        if(SecurePreferences.contains(context!!, "mnemonic")) {

            val address = getCredentialsFromPrivateKey().address

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

    fun getCredentialsFromPrivateKey() : Credentials {

        val mnemonic = SecurePreferences.getStringValue(context!!, "mnemonic", null)
        val seed = MnemonicUtils.generateSeed(mnemonic, null)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val path = intArrayOf(44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0)
        val x = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path)
        return Credentials.create(x)
    }

    fun send(toAddress: String, value: String, message: String?, success: (result: TransactionReceipt) -> Unit, failure: (result: String) -> Unit) {

        val endpoint = "https://dai.poa.network"
        val web3 = Web3j.build(HttpService(endpoint))
        val credentials = getCredentialsFromPrivateKey()

        try {

            val amountEther = BigDecimal.valueOf(value.toDouble())
            val amountWei = Convert.toWei(amountEther, Convert.Unit.ETHER).toBigInteger()

            var gasLimit = GAS_LIMIT
            var extraData = ""
            if(message != null) {
                gasLimit = BigInteger.valueOf(80000L)
                extraData = strToHex(message)
            }

            val ethGetTransactionCount = web3.ethGetTransactionCount(getAddress(), DefaultBlockParameterName.PENDING).send()
            val nonce = ethGetTransactionCount.transactionCount


            val rawTransaction = RawTransaction.createTransaction(nonce, GAS_PRICE, gasLimit, toAddress, amountWei, extraData)
            val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)

            val ethSendTransaction = web3.ethSendRawTransaction(hexValue).send()
            val transactionHash = ethSendTransaction.transactionHash
            var transactionReceipt : EthGetTransactionReceipt

            var numberOfTries = 0
            while (true) {
                transactionReceipt = web3
                    .ethGetTransactionReceipt(transactionHash)
                    .send()
                if (transactionReceipt.result != null) {
                    break
                }  else if(numberOfTries == 5) {
                    throw Exception("Failed executing transaction. Contact our support.")
                }
                Thread.sleep(1000)
                numberOfTries++
            }

            val txReceipt = transactionReceipt

            success(txReceipt.result)

        } catch (e: Exception) {

            Log.d("failed", "sending tx: " + e.toString())

            failure(e.localizedMessage)
        }
    }

    fun strToHex(ascii: String): String {
        // Initialize final String
        var hex = ""

        // Make a loop to iterate through
        // every character of ascii string
        for (i in 0 until ascii.length) {

            // take a char from
            // position i of string
            val ch = ascii[i]

            // cast char to integer and
            // find its ascii value
            val `in` = ch.toInt()

            // change this ascii value
            // integer to hexadecimal value
            val part = Integer.toHexString(`in`)

            // add this hexadecimal value
            // to final string.
            hex += part
        }
        // return the final string hex
        return hex
    }

    fun hexToStr(hex: String): String {
        val buff = ByteBuffer.allocate(hex.length / 2)
        var i = 0
        while (i < hex.length) {
            buff.put(Integer.parseInt(hex.substring(i, i + 2), 16).toByte())
            i += 2
        }
        buff.rewind()
        val cs = Charset.forName("UTF-8")
        val cb = cs.decode(buff)

        return cb.toString()
    }

}