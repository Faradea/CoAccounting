package com.macgavrina.co_accounting.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.DateFormatter
import kotlinx.android.synthetic.main.debts_list_item.view.*

class DebtsRecyclerViewAdapter:
        RecyclerView.Adapter<DebtsRecyclerViewAdapter.ViewHolder>() {

    private var mItems: List<Debt>? = null

    fun setDebts(debts: List<Debt>) {
        this.mItems = debts
        notifyDataSetChanged()
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        //val receiver = view.debts_list_item_receiver_tv
        val amount = view.debts_list_item_amount_tv
        val datetime = view.debts_list_item_datetime_tv
        val comment = view.contacts_list_item_title_tv

        private var mItem: Debt? = null

        init {
            view.setOnClickListener(this)
        }

        fun setItem(item: Debt) {
            mItem = item
        }

        override fun onClick(view: View) {
            MainApplication.bus.send(Events.OnClickDebtItemList(mItem?.uid.toString()))
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DebtsRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.debts_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        val item = mItems?.get(position)

        holder.amount.text = item?.spentAmount
        if (item?.datetime != null && item.datetime!!.isNotEmpty()) {
            holder.datetime.text = DateFormatter().formatDateFromTimestamp(item?.datetime!!.toLong())
        }

        if (item?.comment != null && item.comment!!.isNotEmpty()) {
            holder.comment.text = item.comment
        } else {
            holder.comment.text = "..."
        }
        holder.setItem(mItems?.get(position)!!)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems!!.size
        }
        return -1
    }

    interface OnItemClickListener {
        fun onItemClick(item: Debt)
    }
}