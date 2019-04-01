package com.macgavrina.co_accounting.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.rxjava.Events
import kotlinx.android.synthetic.main.active_currency_list_item.view.*

class DebtCurrenciesRecyclerViewAdapter(inputOnClickListener: OnCurrencyClickListener):
        RecyclerView.Adapter<DebtCurrenciesRecyclerViewAdapter.ViewHolder>() {

    private var mItems: List<Currency>? = null
    private val mOnClickListener: OnCurrencyClickListener = inputOnClickListener
    private var debtHasSelectedCurrency = false

    fun setCurrencies(currencies: List<Currency>) {
        this.mItems = currencies

        if (mItems!= null && mItems!!.isNotEmpty()) {
            mItems!!.forEach { item ->
                if (item.isActiveForCurrentTrip) {
                    debtHasSelectedCurrency = true
                    return@forEach
                }
            }
        }

        notifyDataSetChanged()
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val currencySymbolTV = view.active_currency_list_item_tv
        val currencyLayout = view.active_currency_list_item_layout

        private var mItem: Currency? = null

        init {
        }

        fun setItem(item: Currency) {
            mItem = item
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DebtCurrenciesRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.active_currency_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (mItems == null) return

        val item = mItems?.get(position) ?: return

        holder.currencySymbolTV.text = item.symbol

        if (debtHasSelectedCurrency) {
            if (item.isActiveForCurrentTrip) {
                holder.currencyLayout.setCardBackgroundColor(MainApplication.applicationContext().resources.getColor(R.color.colorSecondary))
            } else {
                holder.currencyLayout.setCardBackgroundColor(MainApplication.applicationContext().resources.getColor(R.color.colorBackground))
            }
        } else {
            if (item.lastUsedCurrencyId < 1 && position == 0) {
                holder.currencyLayout.setCardBackgroundColor(MainApplication.applicationContext().resources.getColor(R.color.colorSecondary))
                MainApplication.bus.send(Events.OnClickCurrencyInDebt(item.uid))
            } else {
                if (item.uid == item.lastUsedCurrencyId) {
                    holder.currencyLayout.setCardBackgroundColor(MainApplication.applicationContext().resources.getColor(R.color.colorSecondary))
                    MainApplication.bus.send(Events.OnClickCurrencyInDebt(item.uid))
                } else {
                    holder.currencyLayout.setCardBackgroundColor(MainApplication.applicationContext().resources.getColor(R.color.colorBackground))
                }
            }
        }

        holder.currencyLayout.setOnClickListener {
            mOnClickListener.onCurrencyClick(item.uid)
        }

        holder.setItem(mItems!![position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (mItems != null) {
            return mItems!!.size
        }
        return -1
    }

    interface OnCurrencyClickListener {
        fun onCurrencyClick(selectedCurrencyId: Int)
    }
}