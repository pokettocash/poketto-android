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
import com.google.gson.Gson
import com.jay.widget.StickyHeadersLinearLayoutManager
import net.glxn.qrgen.android.QRCode
import com.poketto.poketto.R
import com.poketto.poketto.adapters.RecyclerAdapter
import com.poketto.poketto.data.ContactsDAO
import com.poketto.poketto.models.DashboardTransactionViewModel
import com.poketto.poketto.models.Transaction
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private var balanceTextView: TextView? = null
    val IMPORT_SEED = 101
    private lateinit var linearLayoutManager: StickyHeadersLinearLayoutManager<RecyclerAdapter>
    private lateinit var adapter: RecyclerAdapter
    //    private lateinit var sectionAdapter: SectionedRecyclerViewAdapter
    private var transactionsList = ArrayList<Transaction>()
    private var dashboardTransactionsList = ArrayList<DashboardTransactionViewModel>()
    private var contactsDAO : ContactsDAO? = null
    private lateinit var ownerAddress : String
    private val weiToDaiRate = 1000000000000000000

    private val cal = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contactsDAO = ContactsDAO(this)

        linearLayoutManager = StickyHeadersLinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

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

        buy_xDai_button.setOnClickListener {
            val url = "https://buy.carbon.money/?apiKey=9899fb8c-837b-41a5-a8bd-3094b7def049&tokens=xDai&homeScreenMessage=Poketto&receiveAddressxDai=${ownerAddress}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        exchange_dai_button.setOnClickListener {
            val url = "https://medium.com/@jaredstauffer/how-to-get-xdai-how-to-convert-dai-to-xdai-eth-dai-xdai-30a60e4b6641"
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
            val transactions = Transactions()
            transactions.result = transactionsList
            intent.putExtra("TRANSACTIONS", Gson().toJson(transactions))
            intent.putExtra("ownerAddress", ownerAddress)
            startActivity(intent)
        }

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.nav_button_settings)
        toolbar.setNavigationOnClickListener {
            showSettingsModal()
        }

        swipe_refresh.setOnRefreshListener {
            updateWallet()
        }

        balanceTextView = findViewById(R.id.balance_value)
    }

    override fun onResume() {

        updateWallet()

        super.onResume()
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

    private fun updateWallet() {

        doAsync {
            ownerAddress = Wallet(this@MainActivity).getAddress()!!
            
            Log.d("updateWallet", "address: " + ownerAddress)
            val dai = Wallet(this@MainActivity).balanceFrom(ownerAddress)
            Log.d("balance", "dai balance: " + dai)

            uiThread {

                adapter = RecyclerAdapter(dashboardTransactionsList, ownerAddress)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()

                val formattedDaiString = String.format("%.2f", dai)
                balanceTextView!!.text = "$formattedDaiString xDai"

                transactionsFrom(ownerAddress)
            }
        }
    }

    private fun showRequestModal() {

        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.request_modal)
        dialog.setTitle("")
        dialog.setOnDismissListener {
            Log.d("dismiss request", "dismiss request")
            updateWallet()
        }

//        val dialogButton = dialog.findViewById(R.id.dialogButtonOK) as Button
//        dialogButton.setOnClickListener {
//            dialog.dismiss()
//        }

        dialog.show()

        doAsync {

            val address = Wallet(this@MainActivity).getAddress()

            uiThread {

                val addressTextView = dialog.findViewById(R.id.address) as TextView
                addressTextView.text = address

                val copyButton = dialog.findViewById(R.id.copy_layout) as LinearLayout
                copyButton.setOnClickListener {

                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("text", "ethereum:${address}")
                    clipboard?.primaryClip = clip
                    dialog.dismiss()
                }

                val shareButton = dialog.findViewById(R.id.share_layout) as LinearLayout
                shareButton.setOnClickListener {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "ethereum:${address}")
                        type = "text/plain"
                    }
                    startActivity(sendIntent)
                }

                doAsync {

                    val bitmap = QRCode.from(address).withSize(150, 150).bitmap()

                    uiThread {

                        val image = dialog.findViewById(R.id.image) as ImageView
                        image.setImageBitmap(bitmap)
                    }
                }

            }
        }

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
                    } else {
                        empty_state_view.visibility = View.INVISIBLE
                    }

                    val serializedTransactions = getSerializedTransactionsWithContactInfo(transactions.result, address)
                    val reverseSerializedTransactions = serializedTransactions.reversed()
                    val groupedTransactions = getGroupedTransactions(reverseSerializedTransactions)
                    val transactionsViewModels = buildTransactionsViewModels(groupedTransactions)


                    var spentToday = 0F
                    for(transaction in reverseSerializedTransactions) {
                        if(transaction.from!!.toUpperCase() == ownerAddress.toUpperCase()) {
                            val transactionTime = Calendar.getInstance()
                            transactionTime.timeInMillis = transaction.timeStamp!!.toLong()*1000
                            val now = Calendar.getInstance()
                            if (now.get(Calendar.DATE) == transactionTime.get(Calendar.DATE)) {
                                spentToday += transaction.value!!.toFloat()
                            }
                        }
                    }
                    val formattedDaiString = String.format("%.2f", spentToday / weiToDaiRate)
                    spent_today_value!!.text = formattedDaiString


                    runOnUiThread {

                        transactionsList.clear()
                        transactionsList.addAll(reverseSerializedTransactions)

                        dashboardTransactionsList.clear()
                        dashboardTransactionsList.addAll(transactionsViewModels)

                        adapter.notifyItemInserted(dashboardTransactionsList.size)
                        adapter.notifyDataSetChanged()
                        swipe_refresh.isRefreshing = false
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

    private fun getGroupedTransactions(transactions: List<Transaction> ): HashMap<Date, ArrayList<Transaction>>  {

        val groupedTransactions = HashMap<Date, ArrayList<Transaction>>()

        for(transaction in transactions) {

            cal.timeInMillis = transaction.timeStamp!!.toLong()*1000
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            if (!groupedTransactions.containsKey(cal.time)) {
                groupedTransactions[cal.time] = arrayListOf()
            }
            val list = groupedTransactions[cal.time]
            if(list!!.isEmpty()) {
                list.add(0, transaction)
            } else {
                list.add(list.lastIndex, transaction)
            }
            groupedTransactions[cal.time] = list
        }

        return groupedTransactions
    }

    private fun buildTransactionsViewModels(groupedTransactions: HashMap<Date, ArrayList<Transaction>>): ArrayList<DashboardTransactionViewModel> {
        val keys = groupedTransactions.keys
        val transactionsViewModels = arrayListOf<DashboardTransactionViewModel>()
        if(keys.size > 0) {
            for(key in keys.sortedByDescending { it.time }) {
                val date = key
                val transactionViewModel = DashboardTransactionViewModel()
                transactionViewModel.date = date
                transactionViewModel.transaction = null
                transactionsViewModels.add(transactionViewModel)
                val transactions = groupedTransactions[key]
                for(transaction in transactions!!) {
                    val newTransactionViewModel = DashboardTransactionViewModel()
                    newTransactionViewModel.transaction = transaction
                    newTransactionViewModel.date = null
                    transactionsViewModels.add(newTransactionViewModel)
                }
            }
            return transactionsViewModels
        } else {
            return arrayListOf()
        }
    }

    fun getSerializedTransactionsWithContactInfo(transactions: List<Transaction>, ownerAddress: String): ArrayList<Transaction> {

        val serializedTransactions = arrayListOf<Transaction>()

        for(transaction in transactions) {
            var othersAddress = transaction.to
            if(transaction.to!!.toUpperCase() == ownerAddress.toUpperCase()) {
                othersAddress = transaction.from!!.toUpperCase()
            }

            val contact = contactsDAO!!.getContactBy(othersAddress!!)
            if(contact != null) {
                transaction.displayName = contact.name
                if(contact.avatar_url != null) {
                    transaction.displayImage = contact.avatar_url
                }
            }

            serializedTransactions.add(transaction)
        }

        return serializedTransactions
    }


}
