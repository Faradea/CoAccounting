package com.macgavrina.co_accounting.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.MoneyFormatter
import kotlinx.android.synthetic.main.selected_receivers_list_item.view.*

class SelectedReceiversRecyclerViewAdapter (contactsList: List<Contact>?, amountPerPerson: Double) :
        RecyclerView.Adapter<SelectedReceiversRecyclerViewAdapter.ViewHolder>() {

    private val amountPerPerson = amountPerPerson
    private val mItems: List<Contact>? = contactsList

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val receiverName = view.selected_receivers_list_item_name_tv
        val amount = view.selected_receivers_list_item_amount_tv

        private var mItem: Contact? = null

        init {
            view.setOnClickListener(this)
            view.selected_receivers_list_layout.setOnClickListener(this)
        }

        fun setItem(item: Contact) {
            mItem = item
        }

        override fun onClick(view: View) {
            MainApplication.bus.send(Events.OnClickSelectedReceiverOnAddExpenseFragment(mItem!!))
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): SelectedReceiversRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.selected_receivers_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: SelectedReceiversRecyclerViewAdapter.ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        val item = mItems?.get(position)
        holder.amount.text = MoneyFormatter.formatAmountForReadOnlyText(amountPerPerson)
        holder.receiverName.text = item?.alias
        holder.setItem(mItems?.get(position)!!)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems.size
        }
        return -1
    }

}