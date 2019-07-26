package com.poketto.poketto.controllers

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.makeramen.roundedimageview.RoundedImageView
import com.poketto.poketto.models.ContactModel
import com.poketto.poketto.utils.PhoneContactUtils
import kotlinx.android.synthetic.main.activity_contacts.*
import android.widget.*
import org.bouncycastle.asn1.x500.style.RFC4519Style.l
import android.widget.Toast




class ContactsActivity: AppCompatActivity() {

    private var listView: ListView? = null
    private var customAdapter: CustomAdapter? = null
    private var contactModelArrayList: ArrayList<ContactModel>? = null
    private var filteredContactModelArrayList: ArrayList<ContactModel>? = null
    private var phoneContactUtils: PhoneContactUtils? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.poketto.poketto.R.layout.activity_contacts)

        phoneContactUtils = PhoneContactUtils(this)


        val toolbar = findViewById<android.support.v7.widget.Toolbar>(com.poketto.poketto.R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = "Assign address"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

//        val address = intent.getStringExtra("address")

        listView = findViewById(com.poketto.poketto.R.id.listView)

        contactModelArrayList = ArrayList()

        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            val contactModel = ContactModel()
            contactModel.setNames(name)
            contactModel.setNumbers(phoneNumber)
            contactModelArrayList!!.add(contactModel)
            Log.d("name>phoneNumber", name + phoneNumber)
        }
        phones.close()

        filteredContactModelArrayList = arrayListOf()
        filteredContactModelArrayList!!.addAll(contactModelArrayList!!)

        customAdapter = CustomAdapter(this, phoneContactUtils!!, filteredContactModelArrayList!!)
        listView!!.adapter = customAdapter


        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("onQueryTextChange: ", newText)
                val newContactsList = contactModelArrayList!!.filter { l -> l.name!!.contains(newText) }
                val newArrayList = arrayListOf<ContactModel>()
                newArrayList.addAll(newContactsList)
                updateList(newArrayList)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                searchBar.clearFocus()
                return false
            }
        })

    }

    private fun updateList(filteredContactsList: ArrayList<ContactModel>) {
        filteredContactModelArrayList!!.clear()
        filteredContactModelArrayList!!.addAll(filteredContactsList)
        customAdapter!!.notifyDataSetChanged()
    }

    class CustomAdapter(private val context: Context, private val phoneContactUtils : PhoneContactUtils, private val adapterList: ArrayList<ContactModel>) : BaseAdapter() {

        override fun getViewTypeCount(): Int {
            return count
        }

        override fun getItemViewType(position: Int): Int {

            return position
        }

        override fun getCount(): Int {
            return adapterList.size
        }

        override fun getItem(position: Int): Any {
            return adapterList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertedView = convertView
            val holder: ViewHolder

            val contactModel = adapterList[position]

            if (convertedView == null) {
                holder = ViewHolder()
                val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertedView = inflater.inflate(com.poketto.poketto.R.layout.lv_item, null, true)

                holder.contactName = convertedView!!.findViewById(com.poketto.poketto.R.id.name) as TextView

                if(contactModel.number != null) {
                    val contactId = phoneContactUtils.fetchContactIdFromPhoneNumber(contactModel.number!!)
                    val contactImageUri = phoneContactUtils.getPhotoUri(contactId.toLong())
                    holder.contactImageView = convertedView.findViewById(com.poketto.poketto.R.id.itemImage) as RoundedImageView
                    holder.contactImageView!!.setImageURI(contactImageUri)
                }

                convertedView.tag = holder
            } else {
                // the getTag returns the viewHolder object set as a tag to the view
                holder = convertedView.tag as ViewHolder
            }

            holder.contactName!!.text = contactModel.getNames()

            return convertedView
        }

        private inner class ViewHolder {

            var contactName: TextView? = null
            var contactImageView : ImageView? = null
        }
    }
}