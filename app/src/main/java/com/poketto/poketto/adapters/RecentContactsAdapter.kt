package com.poketto.poketto.adapters

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.poketto.poketto.R
import com.poketto.poketto.controllers.PaymentSendActivity
import com.poketto.poketto.controllers.inflate
import com.poketto.poketto.data.Contact
import com.poketto.poketto.utils.PhoneContactUtils
import kotlinx.android.synthetic.main.recent_contact_item_row.view.*


class RecentContactsAdapter(private val contacts: ArrayList<Contact>, private val phoneContactUtils : PhoneContactUtils,
                            private val ownerAddress: String): RecyclerView.Adapter<RecentContactsAdapter.ContactHolder>() {

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val itemContact = contacts[position]
        holder.bindContact(itemContact, phoneContactUtils, ownerAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val inflatedView = parent.inflate(R.layout.recent_contact_item_row, false)
        return ContactHolder(inflatedView)
    }

    class ContactHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view: View = v
        private var contact: Contact? = null
        private var ownerAddress: String? = null

        init {
            v.setOnClickListener(this)
        }


        override fun onClick(v: View) {
            Log.d("Contact", "CLICK!")
            val context = itemView.context
            val paymentSendIntent = Intent(context, PaymentSendActivity::class.java)
            paymentSendIntent.putExtra("address", contact!!.address)
            context.startActivity(paymentSendIntent)
        }

        fun bindContact(contact: Contact, phoneContactUtils : PhoneContactUtils, ownerAddress: String) {
            this.contact = contact
            this.ownerAddress = ownerAddress

            view.contact.text = contact.name

            if(contact.contact_id != null) {
                val contactImageUri = phoneContactUtils.getPhotoUri(contact.contact_id!!.toLong())
                if(contactImageUri != null) {
                    view.itemImage.setImageURI(contactImageUri)
                } else {
                    view.itemImage!!.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.pay_unknown))
                }
            } else {
                view.itemImage!!.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.pay_unknown))
            }
        }

    }
}