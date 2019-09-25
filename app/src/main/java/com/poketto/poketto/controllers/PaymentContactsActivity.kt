package com.poketto.poketto.controllers

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_payment_contacts.*
import android.Manifest.permission
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import com.google.gson.Gson
import com.poketto.poketto.adapters.PopularContactsAdapter
import com.poketto.poketto.adapters.RecentContactsAdapter
import com.poketto.poketto.data.Contact
import com.poketto.poketto.data.ContactsDAO
import com.poketto.poketto.models.Transactions
import com.poketto.poketto.utils.PhoneContactUtils
import kotlinx.android.synthetic.main.activity_payment_contacts.searchBar

const val CAMERA_PERMISSION = 1001

class PaymentContactsActivity : AppCompatActivity() {

    val QRCODE = 1002
    private var phoneContactUtils: PhoneContactUtils = PhoneContactUtils(this)
    private lateinit var filteredPaymentContacts : ArrayList<Contact>
    private lateinit var recentPaymentContacts : ArrayList<Contact>
    private lateinit var popularPaymentContacts : ArrayList<Contact>
    private lateinit var recentContactsAdapter: RecentContactsAdapter
    private lateinit var popularContactsAdapter: PopularContactsAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var popularLinearLayoutManager: LinearLayoutManager


    lateinit var transactions : Transactions
    lateinit var ownerAddress : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.poketto.poketto.R.layout.activity_payment_contacts)

        linearLayoutManager = LinearLayoutManager(this)
        popularLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recent_list.layoutManager = linearLayoutManager
        popular_list.layoutManager = popularLinearLayoutManager

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(com.poketto.poketto.R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val transactionJson = intent.getStringExtra("TRANSACTIONS")
        transactions = Gson().fromJson(transactionJson, Transactions::class.java)
        ownerAddress = intent.getStringExtra("ownerAddress")

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("onQueryTextChange: ", newText)
                Log.d("onQueryTextChange: ", "recentPaymentContacts" + recentPaymentContacts)

                val newContactsList = recentPaymentContacts.filter { l -> l.name!!.startsWith(newText) }
                val newArrayList = arrayListOf<Contact>()
                newArrayList.addAll(newContactsList)
                updateList(newArrayList)
                if(newText == "") {
                    if(popularPaymentContacts.isNotEmpty()) {
                        popular_layout.visibility = View.VISIBLE
                    }
                    clipboard_layout.visibility = View.VISIBLE
                    scan_layout.visibility = View.VISIBLE
                } else {
                    clipboard_layout.visibility = View.GONE
                    scan_layout.visibility = View.GONE
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                searchBar.clearFocus()
                return false
            }
        })

        searchBar.setOnClickListener {
            searchBar.isIconified = false
        }

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

        val pasteAddressTextView = findViewById<TextView>(com.poketto.poketto.R.id.address_text_view)

        if(clipboard != null && clipboard.primaryClip != null) {
            if(clipboard.primaryClip!!.getItemAt(0).text.startsWith("0x")) {
                pasteAddressTextView.text = clipboard.primaryClip!!.getItemAt(0).text
            } else if (clipboard.primaryClip!!.getItemAt(0).text.startsWith("ethereum:")) {
                pasteAddressTextView.text = clipboard.primaryClip!!.getItemAt(0).text.substring(9)
            }
        }

        clipboard_layout.setOnClickListener {
            if(clipboard != null && clipboard.primaryClip != null) {
                if (clipboard.primaryClip!!.getItemAt(0).text.startsWith("0x")) {
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", clipboard.primaryClip!!.getItemAt(0).text)
                    startActivity(intent)
                } else if (clipboard.primaryClip!!.getItemAt(0).text.startsWith("ethereum:")) {
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", clipboard.primaryClip!!.getItemAt(0).text.substring(9))
                    startActivity(intent)
                }
            }
        }

        scan_layout.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), CAMERA_PERMISSION)
            } else {
                val intent = Intent(this, CustomQRCodeActivity::class.java)
                startActivityForResult(intent, QRCODE)
            }
        }

        setPaymentContacts()
    }

    override
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, CustomQRCodeActivity::class.java)
                    startActivityForResult(intent, QRCODE)
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == QRCODE) {

            if(resultCode == RESULT_OK) {
                val scannedResult = data!!.getStringExtra("address")

                if(scannedResult.startsWith("0x")) {
                    Log.d("address", "address: " + scannedResult)
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", scannedResult)
                    startActivity(intent)
                } else if(scannedResult.startsWith("ethereum:")) {
                    val address = scannedResult.substring(9)
                    Log.d("address", "address: " + address)
                    val intent = Intent(this, PaymentSendActivity::class.java)
                    intent.putExtra("address", address)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@PaymentContactsActivity, "Invalid address", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun setPaymentContacts() {

        val paymentContactsArray : ArrayList<Contact> = arrayListOf()
        val popularContactsArray : ArrayList<Contact> = arrayListOf()

        var toTransactions : ArrayList<String> = arrayListOf()
        for(transaction in transactions.result) {
            toTransactions.add(transaction.to!!)
        }

        var fromTransactions : ArrayList<String> = arrayListOf()
        for(transaction in transactions.result) {
            fromTransactions.add(transaction.from!!)
        }

        toTransactions = ArrayList(toTransactions.toList().filterNot { it.toUpperCase() == ownerAddress.toUpperCase() })
        fromTransactions = ArrayList(fromTransactions.toList().filterNot { it.toUpperCase() == ownerAddress.toUpperCase() })

        val allTransactions = toTransactions
        allTransactions.addAll(fromTransactions)
        Log.d("allTransactions", "allTransactions: " + allTransactions)


        val uniqueTransactions = allTransactions.distinct()
        Log.d("uniqueTransactions", "uniqueTransactions: " + uniqueTransactions)

        val popularTransactions = allTransactions.groupBy { it }.filter { it.component2().size > 1 }.keys
        Log.d("popularTransactions", "popularTransactions: " + popularTransactions)


        for(transaction in uniqueTransactions) {
            if(paymentContactsArray.isEmpty()) {
                val paymentContact = addContact(transaction)
                paymentContactsArray.add(paymentContact)
            } else {
                var filteredContacts = paymentContactsArray.filter { it.address!!.toUpperCase() == transaction.toUpperCase() }
                if(filteredContacts.isEmpty()) {
                    val contact = ContactsDAO(this).getContactBy(transaction.toUpperCase())
                    filteredContacts = paymentContactsArray.filter { it.address!!.toUpperCase() == contact?.address?.toUpperCase() }
                    if(filteredContacts.isEmpty()) {
                        val paymentContact = addContact(transaction)
                        paymentContactsArray.add(paymentContact)
                    }
                } else {
                    val paymentContact = addContact(transaction)
                    paymentContactsArray.add(paymentContact)
                }
            }
        }

        for(transaction in popularTransactions) {
            if(popularContactsArray.isEmpty()) {
                val paymentContact = addContact(transaction)
                popularContactsArray.add(paymentContact)
            } else {
                var filteredContacts = popularContactsArray.filter { it.address!!.toUpperCase() == transaction.toUpperCase() }
                if(filteredContacts.isEmpty()) {
                    val contact = ContactsDAO(this).getContactBy(transaction.toUpperCase())
                    filteredContacts = popularContactsArray.filter { it.address!!.toUpperCase() == contact?.address?.toUpperCase() }
                    if(filteredContacts.isEmpty()) {
                        val paymentContact = addContact(transaction)
                        popularContactsArray.add(paymentContact)
                    }
                } else {
                    val paymentContact = addContact(transaction)
                    popularContactsArray.add(paymentContact)
                }
            }
        }


        recentPaymentContacts = paymentContactsArray
        filteredPaymentContacts = arrayListOf()
        filteredPaymentContacts.clear()
        filteredPaymentContacts.addAll(recentPaymentContacts)
        Log.d("filteredPaymentContacts", "filteredPaymentContacts: " + recentPaymentContacts)
        popularPaymentContacts = popularContactsArray
        Log.d("popularPaymentContacts", "popularPaymentContacts: " + popularPaymentContacts)

        recentContactsAdapter =
            RecentContactsAdapter(filteredPaymentContacts, phoneContactUtils, ownerAddress)
        recent_list.adapter = recentContactsAdapter
        recentContactsAdapter.notifyDataSetChanged()

        popularContactsAdapter =
            PopularContactsAdapter(popularPaymentContacts, phoneContactUtils, ownerAddress)
        popular_list.adapter = popularContactsAdapter
        popularContactsAdapter.notifyDataSetChanged()
        if(popularPaymentContacts.isNotEmpty()) {
            popular_layout.visibility = View.VISIBLE
        }
    }

    private fun updateList(filteredContactsList: ArrayList<Contact>) {
        Log.d("updateList: ", "contacts: " + filteredContactsList)

        filteredPaymentContacts.clear()
        filteredPaymentContacts.addAll(filteredContactsList)
        recentContactsAdapter.notifyDataSetChanged()
        if(filteredPaymentContacts.isEmpty()) {
            recent_list.visibility = View.GONE
        } else {
            recent_list.visibility = View.VISIBLE
        }
    }


    fun addContact(address: String): Contact {
        val paymentContact = Contact()

        val contact = ContactsDAO(this).getContactBy(address.toUpperCase())
        if (contact != null) {
            paymentContact.address = address
            paymentContact.name = contact.name
            paymentContact.contact_id = contact.contact_id
        } else {
            paymentContact.address = address
            paymentContact.name = address
        }

        return paymentContact
    }
}