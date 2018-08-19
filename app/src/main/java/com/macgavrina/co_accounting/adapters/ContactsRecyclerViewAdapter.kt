package com.macgavrina.co_accounting.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import kotlinx.android.synthetic.main.contacts_list_item.view.*

class ContactsRecyclerViewAdapter (contactsList: List<Contact>?) :
        RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder>() {

    private val mItems: List<Contact>? = contactsList

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val friendAliasTV = view.contacts_list_item_alias_tv
        val friendEmailTV = view.contacts_list_item_email_tv

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
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems.size
        }
        return -1
    }

}