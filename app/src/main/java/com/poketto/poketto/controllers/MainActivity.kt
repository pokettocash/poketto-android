package com.poketto.poketto.controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.poketto.poketto.api.RetrofitInitializer
import com.poketto.poketto.models.Transactions
import com.poketto.poketto.services.Wallet
import de.adorsys.android.securestoragelibrary.SecurePreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.Menu
import android.text.Spannable
import android.text.style.ImageSpan
import android.text.SpannableString
import android.view.View
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.MenuItem
import android.widget.*
import net.glxn.qrgen.android.QRCode
import com.poketto.poketto.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var balanceTextView: TextView? = null
    val IMPORT_SEED = 101


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

        receive_funds_button.setOnClickListener {
            showRequestModal()
        }

        add_funds_button.setOnClickListener {
            val url = "https://poketto.cash"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
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
        payButton.setOnClickListener {
            val intent = Intent(this, PaymentContactsActivity::class.java)
            startActivity(intent)
        }

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.nav_button_settings)
        toolbar.setNavigationOnClickListener {
            showSettingsModal()
        }

        balanceTextView = findViewById(R.id.balance_value)

        updateWallet()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == IMPORT_SEED) {
            updateWallet()
        }
    }

    fun updateWallet() {

        val address = Wallet(this).getAddress()
        Log.d("updateWallet", "address: " + address)
        val dai = Wallet(this).balanceFrom(address!!)
        Log.d("balance", "dai balance: " + dai)
        val formattedDaiString = String.format("%.2f", dai)
        balanceTextView!!.text = "$formattedDaiString xDai"
        transactionsFrom(address)
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

        val copyButton = dialog.findViewById(R.id.copy_layout) as LinearLayout
        copyButton.setOnClickListener {

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("text", address)
            clipboard?.primaryClip = clip
            dialog.dismiss()
        }

        val shareButton = dialog.findViewById(R.id.share_layout) as LinearLayout
        shareButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, address)
                type = "text/plain"
            }
            startActivity(sendIntent)
        }

//        val dialogButton = dialog.findViewById(R.id.dialogButtonOK) as Button
//        dialogButton.setOnClickListener {
//            dialog.dismiss()
//        }

        dialog.show()
    }

    private fun showSettingsModal() {

        Log.d("showSettingsModal", "showSettingsModal")

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.settings_modal)
        dialog.setTitle("")

        val twitterButton = dialog.findViewById(R.id.settings_twitter) as ImageButton
        twitterButton.setOnClickListener {
            val url = "https://twitter.com/pokettocash"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val discordButton = dialog.findViewById(R.id.settings_discord) as ImageButton
        discordButton.setOnClickListener {
            val url = "https://discord.gg/kMTUpME"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val githubButton = dialog.findViewById(R.id.settings_github) as ImageButton
        githubButton.setOnClickListener {
            val url = "https://github.com/pokettocash"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val legalButton = dialog.findViewById(R.id.settings_legal_layout) as LinearLayout
        legalButton.setOnClickListener {
            val url = "https://github.com/pokettocash/poketto-ios/blob/master/LICENSE"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val backupButton = dialog.findViewById(R.id.backup_layout) as LinearLayout
        backupButton.setOnClickListener {
            val mnemonic = SecurePreferences.getStringValue(this, "mnemonic", null)
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, mnemonic)
                type = "text/plain"
            }
            startActivity(sendIntent)
        }

        val seedButton = dialog.findViewById(R.id.seed_layout) as LinearLayout
        seedButton.setOnClickListener {

            val intent = Intent(this, SeedActivity::class.java)
            startActivityForResult(intent, IMPORT_SEED)
        }

        val versionNumberTextView = dialog.findViewById(R.id.app_version) as TextView
        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionNumberTextView.text = "${info.versionName}(${info.versionCode})"

        dialog.show()
    }

    private fun transactionsFrom(address: String) {

        val call = RetrofitInitializer().explorer().transactions("account", "txlist", address)
        call.enqueue(object: Callback<Transactions?> {
                override fun onResponse(call: Call<Transactions?>?,
                                        response: Response<Transactions?>?) {
                    response?.body()?.let {
                        val transactions: Transactions = it
                        Log.d("onResponse", "transactions: " + transactions.result)
                        launching_view.visibility = View.INVISIBLE
                        if(transactions.result.isEmpty()) {
                            empty_state_view.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<Transactions?>?,
                                       t: Throwable?) {
                    Log.d("onFailure", "transactions: " + t?.localizedMessage)
                    launching_view.visibility = View.INVISIBLE
                }
            }
        )
    }



}
