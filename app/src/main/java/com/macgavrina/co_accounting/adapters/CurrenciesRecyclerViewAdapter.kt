package com.macgavrina.co_accounting.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.rxjava.Events
import kotlinx.android.synthetic.main.contacts_list_item.view.*
import kotlinx.android.synthetic.main.currency_list_item.view.*

class CurrenciesRecyclerViewAdapter():
        RecyclerView.Adapter<CurrenciesRecyclerViewAdapter.ViewHolder>() {

    private var mItems: List<Currency>? = null

    fun setCurrencies(currencies: List<Currency>) {
        this.mItems = currencies
        notifyDataSetChanged()
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val currencySymbolTV = view.currency_list_item_symbol
        val currencyNameTV = view.currency_list_item_name
        val currencyCheckBox = view.currency_list_item_checkBox

        private var mItem: Currency? = null

        init {
//            view.setOnClickListener{
//                MainApplication.bus.send(Events.OnClickCurrency(mItem?.uid))
//            }

            currencyCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView != null && mItem != null && mItem!!.uid != null) {
                    Log.d("you are touching my checkbox, isChecked = $isChecked")
                    MainApplication.bus.send(Events.OnClickCheckboxCurrency(mItem!!.uid, isChecked))
                }
            }
        }

        fun setItem(item: Currency) {
            mItem = item
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CurrenciesRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.currency_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (mItems == null) return

        val item = mItems?.get(position) ?: return

        Log.d("binding item, item.name = ${item.name}, item.activeTripId = ${item.activeTripId}")
        holder.currencySymbolTV.text = item.symbol
        holder.currencyNameTV.text = item.name
        holder.currencyCheckBox.isChecked = (item.activeTripId > 0)

        holder.setItem(mItems!![position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems!!.size
        }
        return -1
    }
}