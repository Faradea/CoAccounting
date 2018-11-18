package com.macgavrina.co_accounting.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.rxjava.Events
import kotlinx.android.synthetic.main.not_selected_receivers_list_item.view.*

class NotSelectedReceiversRecyclerViewAdapter (contactsList: List<Contact>?) :
        RecyclerView.Adapter<NotSelectedReceiversRecyclerViewAdapter.ViewHolder>() {

    private val mItems: List<Contact>? = contactsList

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val receiverName = view.not_selected_receivers_list_item_tv

        private var mItem: Contact? = null

        init {
            view.setOnClickListener(this)
            view.not_selected_receivers_list_item_tv.setOnClickListener(this)
        }

        fun setItem(item: Contact) {
            mItem = item
        }

        override fun onClick(view: View) {

            Log.d( "onClick ${mItem?.uid}")
            MainApplication.bus.send(Events.NewContactIsAddedToSelectedReceiversList(mItem))
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): NotSelectedReceiversRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.not_selected_receivers_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: NotSelectedReceiversRecyclerViewAdapter.ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Log.d("Bind item with position = ${position}")
        val item = mItems?.get(position)
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