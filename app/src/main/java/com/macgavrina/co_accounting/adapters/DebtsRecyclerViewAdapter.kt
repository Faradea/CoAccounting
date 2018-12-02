package com.macgavrina.co_accounting.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events
import kotlinx.android.synthetic.main.debts_list_item.view.*

class DebtsRecyclerViewAdapter (debtsList: List<Debt>?) :
        RecyclerView.Adapter<DebtsRecyclerViewAdapter.ViewHolder>() {

    private val mItems: List<Debt>? = debtsList

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        //val receiver = view.debts_list_item_receiver_tv
        val amount = view.debts_list_item_amount_tv
        val datetime = view.debts_list_item_datetime_tv
        val comment = view.debts_list_item_comment_tv

        private var mItem: Debt? = null

        init {
            view.setOnClickListener(this)
        }

        fun setItem(item: Debt) {
            mItem = item
        }

        override fun onClick(view: View) {
            Log.d( "onClick ${mItem?.uid}")
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

        Log.d("Bind item with position = ${position}")
        val item = mItems?.get(position)

        holder.amount.text = item?.spentAmount
        holder.datetime.text = item?.datetime
        holder.comment.text = item?.comment
        holder.setItem(mItems?.get(position)!!)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems.size
        }
        return -1
    }

    interface OnItemClickListener {
        fun onItemClick(item: Debt)
    }
}