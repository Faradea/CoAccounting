package com.macgavrina.co_accounting.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.RecieverWithAmount
import kotlinx.android.synthetic.main.add_receiver_list_item.view.*

class AddRecieverRecyclerViewAdapter (receiverWithAmountList: List<RecieverWithAmount>?, inputContactsList: Array<String?>) :
        RecyclerView.Adapter<AddRecieverRecyclerViewAdapter.ViewHolder>() {

    private val mItems: List<RecieverWithAmount>? = receiverWithAmountList
    private val contactsList = inputContactsList

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val recieverSpinner = view.add_receiver_list_item_spinner
        val amountEditText = view.add_receiver_list_item_edittext

        private var mItem: RecieverWithAmount? = null

        init {
            view.setOnClickListener(this)
        }

        fun setItem(item: RecieverWithAmount) {
            mItem = item
        }

        override fun onClick(view: View) {

            //ToDo use some unic id instead of name
            Log.d( "onClick ${mItem?.receiverName}")
            //MainApplication.bus.send(Events.OnClickContactList(mItem?.uid.toString()))
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AddRecieverRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.add_receiver_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Log.d("Bind item with position = ${position}")
        val item = mItems?.get(position)

        val adapter = ArrayAdapter<String>(
                MainApplication.applicationContext(),
                android.R.layout.simple_spinner_item,
                contactsList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.recieverSpinner.adapter = adapter

        holder.amountEditText.setText("100.0")
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
        fun onItemClick(item: RecieverWithAmount)
    }

}