package com.poketto.poketto.services

import android.content.Context
import android.util.Log
import de.adorsys.android.securestoragelibrary.SecurePreferences
import de.adorsys.android.securestoragelibrary.SecureStorageException
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash
import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom

class Wallet(context: Context) {

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

    private fun handleException(e: SecureStorageException) {
        Log.e("handleException", e.message)
    }
}