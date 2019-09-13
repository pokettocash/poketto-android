package com.poketto.poketto.controllers

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.poketto.poketto.models.Transaction
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import android.net.Uri
import com.poketto.poketto.R


class RecyclerAdapter(private val transactions: ArrayList<Transaction>, private val ownerAddress: String): RecyclerView.Adapter<RecyclerAdapter.TransactionHolder>() {

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: RecyclerAdapter.TransactionHolder, position: Int) {
        val itemTransaction = transactions[position]
        holder.bindTransaction(itemTransaction, ownerAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.TransactionHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return TransactionHolder(inflatedView)
    }

    class TransactionHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view: View = v
        private var transaction: Transaction? = null
        private val weiToDaiRate = 1000000000000000000
        private var ownerAddress: String? = null

        init {
            v.setOnClickListener(this)
        }


        override fun onClick(v: View) {
            Log.d("RecyclerView", "CLICK!")
            val context = itemView.context
            val paymentDetailsIntent = Intent(context, PaymentDetailsActivity::class.java)
            paymentDetailsIntent.putExtra(TRANSACTION_KEY, Gson().toJson(transaction))
            paymentDetailsIntent.putExtra("ownerAddress", ownerAddress)
            context.startActivity(paymentDetailsIntent)
        }

        companion object {
            private val TRANSACTION_KEY = "TRANSACTION"
        }

        fun bindTransaction(transaction: Transaction, ownerAddress: String) {
            this.transaction = transaction
            this.ownerAddress = ownerAddress

            val formattedDaiString = String.format("%.2f", transaction.value!!.toDouble()/weiToDaiRate)

            if(transaction.from == ownerAddress) {
                view.address.text = "${transaction.to!!.take(6)}..."
                view.amount.text = "$formattedDaiString"
            } else {
                view.address.text = "${transaction.from!!.take(6)}..."
                view.amount.text = "+ $formattedDaiString"
                view.amount.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorAccent))
            }

            if(transaction.displayName != null) {
                view.contact.text = transaction.displayName
                view.address.text = ""
            }

            if(transaction.displayImage != null) {
                view.itemImage!!.setImageURI(Uri.parse(transaction.displayImage))
            } else {
                view.itemImage!!.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.pay_unknown))
            }
        }

    }

}