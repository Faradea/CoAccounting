package com.macgavrina.co_accounting.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.rxjava.Events
import kotlinx.android.synthetic.main.not_selected_receivers_list_item.view.*

class NotSelectedReceiversWithOnClickRecyclerViewAdapter (contactsList: List<Contact>?, inputOnClickListener: OnNotSelectedContactClickListener) :
        RecyclerView.Adapter<NotSelectedReceiversWithOnClickRecyclerViewAdapter.ViewHolder>() {

    private val mItems: List<Contact>? = contactsList
    private val mOnClickListener: OnNotSelectedContactClickListener = inputOnClickListener

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val receiverName = view.not_selected_receivers_list_item_tv
        val layout = view.not_selected_receivers_list_item_layout

        private var mItem: Contact? = null

        init {
        }

        fun setItem(item: Contact) {
            mItem = item
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): NotSelectedReceiversWithOnClickRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.not_selected_receivers_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: NotSelectedReceiversWithOnClickRecyclerViewAdapter.ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        val item = mItems?.get(position) ?: return
        holder.receiverName.text = item?.alias
        holder.setItem(mItems?.get(position)!!)
        holder.layout.setOnClickListener {
            mOnClickListener.onNotSelectedContactClick(item)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems.size
        }
        return -1
    }

    interface OnNotSelectedContactClickListener {
        fun onNotSelectedContactClick(selectedContact: Contact)
    }

}