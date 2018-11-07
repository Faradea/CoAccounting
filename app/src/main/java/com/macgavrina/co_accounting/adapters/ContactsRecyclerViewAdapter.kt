package com.macgavrina.co_accounting.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import kotlinx.android.synthetic.main.contacts_list_item.view.*
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.rxjava.Events


class ContactsRecyclerViewAdapter (contactsList: List<Contact>?) :
        RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder>() {

    private val mItems: List<Contact>? = contactsList

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val friendAliasTV = view.contacts_list_item_alias_tv
        val friendEmailTV = view.debts_list_item_sender_tv
        private var mItem: Contact? = null

        init {
            view.setOnClickListener(this)
        }

        fun setItem(item: Contact) {
            mItem = item
        }

        override fun onClick(view: View) {
            Log.d( "onClick ${mItem?.uid}")
            MainApplication.bus.send(Events.OnClickContactList(mItem?.uid.toString()))
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ContactsRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.contacts_list_item, parent, false)
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
        holder.friendAliasTV.text = item?.alias
        holder.friendEmailTV.text = item?.email
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
        fun onItemClick(item: Contact)
    }
}