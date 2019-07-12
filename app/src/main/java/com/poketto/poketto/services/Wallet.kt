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
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import java.math.BigDecimal


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

    fun send(toAddress: String, value: String, success: (result: TransactionReceipt) -> Unit, failure: (result: String) -> Unit) {

        val endpoint = "https://dai.poa.network"
        val web3 = Web3j.build(HttpService(endpoint))
        val credentials = getCredentialsFromPrivateKey()

        try {

            val transactionManager : TransactionManager = RawTransactionManager(web3, credentials)
            val transfer = Transfer(web3, transactionManager)
            val transactionReceipt = transfer.sendFunds(toAddress, BigDecimal.valueOf(value.toDouble()), Convert.Unit.ETHER, GAS_PRICE, GAS_LIMIT).sendAsync()

            Log.d("send", "got tx receipt: " + transactionReceipt.get())

            success(transactionReceipt.get())

        } catch (e: Exception) {

            failure(e.localizedMessage)
        }
    }

}