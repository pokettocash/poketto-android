package com.poketto.poketto.adapters

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
import android.text.format.DateFormat
import com.jay.widget.StickyHeaders
import com.poketto.poketto.models.DashboardTransactionViewModel
import com.poketto.poketto.controllers.PaymentDetailsActivity
import com.poketto.poketto.controllers.inflate
import kotlinx.android.synthetic.main.recyclerview_header_row.view.*
import java.util.*
import kotlin.collections.ArrayList
import java.sql.Timestamp


class RecyclerAdapter(private val transactions: ArrayList<DashboardTransactionViewModel>, private val ownerAddress: String): RecyclerView.Adapter<RecyclerAdapter.TransactionHolder>(), StickyHeaders {

    private val LAYOUT_HEADER = 0
    private val LAYOUT_CHILD = 1

    override fun isStickyHeader(p0: Int): Boolean {
        if(transactions.size > 0 && p0 < transactions.size) {
            return transactions[p0].date != null
        }
        return false
    }

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val itemTransaction = transactions[position]
        if(itemTransaction.date != null) {
            holder.bindTransaction(null, ownerAddress, itemTransaction.date)
        } else {
            holder.bindTransaction(itemTransaction.transaction, ownerAddress, null)
        }
    }

    override fun getItemViewType(position: Int): Int {
        when(transactions[position].date != null) {
            true -> return LAYOUT_HEADER
            false -> return LAYOUT_CHILD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        if(viewType == LAYOUT_HEADER) {
            val inflatedView = parent.inflate(com.poketto.poketto.R.layout.recyclerview_header_row, false)
            return TransactionHolder(inflatedView)
        } else {
            val inflatedView = parent.inflate(com.poketto.poketto.R.layout.recyclerview_item_row, false)
            return TransactionHolder(inflatedView)
        }
    }

    class TransactionHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view: View = v
        private var transaction: Transaction? = null
        private val weiToDaiRate = 1000000000000000000
        private var ownerAddress: String? = null
        private var date: Date? = null

        init {
            v.setOnClickListener(this)
        }


        override fun onClick(v: View) {
            Log.d("RecyclerView", "CLICK!")
            if(transaction != null) {
                val context = itemView.context
                val paymentDetailsIntent = Intent(context, PaymentDetailsActivity::class.java)
                paymentDetailsIntent.putExtra(TRANSACTION_KEY, Gson().toJson(transaction))
                paymentDetailsIntent.putExtra("ownerAddress", ownerAddress)
                context.startActivity(paymentDetailsIntent)
            }
        }

        companion object {
            private val TRANSACTION_KEY = "TRANSACTION"
        }

        fun bindTransaction(transaction: Transaction?, ownerAddress: String, date: Date?) {
            this.transaction = transaction
            this.ownerAddress = ownerAddress
            this.date = date

            if(date != null) {
                val now = Calendar.getInstance()
                val dateTime = Calendar.getInstance()
                val ts = Timestamp(date.time)
                dateTime.time = ts

                if (now.get(Calendar.DATE) == dateTime.get(Calendar.DATE) ) {
                    view.header_date_title.text = "Today"
                } else if (now.get(Calendar.DATE) - dateTime.get(Calendar.DATE) == 1){
                    view.header_date_title.text = "Yesterday"
                } else if (now.get(Calendar.WEEK_OF_YEAR) == dateTime.get(Calendar.WEEK_OF_YEAR)) {
                    view.header_date_title.text = DateFormat.format("EEE, dd MMMM", dateTime).toString()
                } else {
                    view.header_date_title.text = DateFormat.format("dd MMMM", dateTime).toString()
                }

            } else {
                val formattedDaiString = String.format("%.2f", transaction!!.value!!.toDouble()/weiToDaiRate)

                if(transaction.from == ownerAddress) {
                    view.address.text = "${transaction!!.to!!.take(6)}..."
                    view.amount.text = "$formattedDaiString"
                } else {
                    view.address.text = "${transaction!!.from!!.take(6)}..."
                    view.amount.text = "+ $formattedDaiString"
                    view.amount.setTextColor(ContextCompat.getColor(itemView.context, com.poketto.poketto.R.color.colorAccent))
                }

                if(transaction!!.displayName != null) {
                    view.contact.text = transaction.displayName
                    view.address.text = ""
                }

                if(transaction.displayImage != null) {
                    view.itemImage!!.setImageURI(Uri.parse(transaction.displayImage))
                } else {
                    view.itemImage!!.setImageDrawable(ContextCompat.getDrawable(itemView.context, com.poketto.poketto.R.drawable.pay_unknown))
                }
            }
        }

    }

}