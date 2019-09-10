package com.poketto.poketto.controllers

import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.poketto.poketto.R
import com.poketto.poketto.data.Contact


class RecentContactsAdapter(private val contacts: ArrayList<Contact>, private val ownerAddress: String): RecyclerView.Adapter<RecentContactsAdapter.ContactHolder>() {

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val itemContact = contacts[position]
        holder.bindContact(itemContact, ownerAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val inflatedView = parent.inflate(R.layout.recent_contact_item_row, false)
        return ContactHolder(inflatedView)
    }

    class ContactHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view: View = v
        private var contact: Contact? = null
        private val weiToDaiRate = 1000000000000000000
        private var ownerAddress: String? = null

        init {
            v.setOnClickListener(this)
        }


        override fun onClick(v: View) {
            Log.d("Contact", "CLICK!")
            val context = itemView.context
            val paymentDetailsIntent = Intent(context, PaymentDetailsActivity::class.java)
            paymentDetailsIntent.putExtra(CONTACT_KEY, Gson().toJson(contact))
            paymentDetailsIntent.putExtra("ownerAddress", ownerAddress)
            context.startActivity(paymentDetailsIntent)
        }

        companion object {
            private val CONTACT_KEY = "CONTACT"
        }

        fun bindContact(contact: Contact, ownerAddress: String) {
            this.contact = contact
            this.ownerAddress = ownerAddress

//            val formattedDaiString = String.format("%.2f", transaction.value!!.toDouble()/weiToDaiRate)
//
//            if(transaction.from == ownerAddress) {
//                view.address.text = transaction.to
//                view.amount.text = "$formattedDaiString"
//            } else {
//                view.address.text = transaction.from
//                view.amount.text = "+ $formattedDaiString"
//                view.amount.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorAccent))
//            }
//
//            if(transaction.displayName != null) {
//                view.contact.text = transaction.displayName
//                view.address.text = ""
//            }
//
//            if(transaction.displayImage != null) {
//                view.itemImage!!.setImageURI(Uri.parse(transaction.displayImage))
//            } else {
//                view.itemImage!!.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.pay_unknown))
//            }
        }

    }
}