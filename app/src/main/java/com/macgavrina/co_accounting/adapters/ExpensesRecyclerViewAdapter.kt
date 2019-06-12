
package com.macgavrina.co_accounting.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.MoneyFormatter
import kotlinx.android.synthetic.main.add_receiver_list_item.view.*


class ExpensesRecyclerViewAdapter(inputOnClickListener: OnExpenseInDebtClickListener) :
        RecyclerView.Adapter<ExpensesRecyclerViewAdapter.ViewHolder>() {

    private var mItems: List<Expense>? = null
    private val mOnClickListener: OnExpenseInDebtClickListener = inputOnClickListener

    fun setExpenses(expenses: List<Expense>) {
        this.mItems = expenses
        notifyDataSetChanged()
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val receiversList = view.add_receiver_list_item_receivers_list_tv
        val amount = view.add_receiver_list_item_amount_tv
        val comment = view.add_receiver_list_item_comment_tv
        val layout = view.add_receiver_list_item_layout

        private var mItem: Expense? = null

        init {
        }

        fun setItem(item: Expense) {
            mItem = item
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ExpensesRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.add_receiver_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        val item = mItems?.get(position)
        holder.receiversList.text = item?.receiversList
        holder.amount.text = MoneyFormatter.formatAmountForReadOnlyText(item?.totalAmount ?: 0.0)

        if (item?.comment.isNullOrEmpty()) {
            holder.comment.text = "..."
        } else {
            holder.comment.text = item?.comment
        }

        holder.setItem(mItems?.get(position)!!)

        holder.layout.setOnClickListener {
            if (item == null) return@setOnClickListener

            Log.d("onClick expense with id = ${item.uid} and debtId = ${item.debtId}")
            mOnClickListener.onExpenseClick(item)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems!!.size
        }
        return -1
    }

    interface OnExpenseInDebtClickListener {
        fun onExpenseClick(expense: Expense)
    }
}
