package com.macgavrina.co_accounting.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Calculation
import com.macgavrina.co_accounting.support.MoneyFormatter
import kotlinx.android.synthetic.main.calculation_list_item.view.*


class CalculationsRecyclerViewAdapter:
        RecyclerView.Adapter<CalculationsRecyclerViewAdapter.ViewHolder>() {

    private var mItems: List<Calculation>? = null

    fun setCalculations(calculations: List<Calculation>) {
        this.mItems = calculations
        notifyDataSetChanged()
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val contactAliasTV = view.calculation_list_item_contactname_tv
        val amount = view.calculation_list_item_amount_tv
        val statusDesc = view.calculation_list_item_status_desc_tv

        private var mItem: Calculation? = null

//        init {
//            view.setOnClickListener{
//                MainApplication.bus.send(Events.OnClickContactList(mItem?.uid.toString()))
//            }
//        }

        fun setItem(item: Calculation) {
            mItem = item
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CalculationsRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.calculation_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (mItems == null) return

        val item = mItems?.get(position) ?: return

        holder.contactAliasTV.text = item.contactAlias

        Log.d("Bind item = $item, currencySymbol = ${item.currencySymbol}")

        //val amountRounded = String.format("%.2f", item.totalAmount)
        Log.d("item.totalAmount = ${item.totalAmount}")
        holder.amount.text = "${MoneyFormatter.formatAmountForReadOnlyText(item.totalAmount)} ${item.currencySymbol}"

        if (item.totalAmount > 0) {
            holder.statusDesc.text = "You are owed"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.statusDesc.setTextColor(MainApplication.applicationContext().getColor(R.color.colorOwed))
                holder.amount.setTextColor(MainApplication.applicationContext().getColor(R.color.colorOwed) )
            }
        } else {
            holder.statusDesc.text = "You owe"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.statusDesc.setTextColor(MainApplication.applicationContext().getColor(R.color.colorOwes))
                holder.amount.setTextColor(MainApplication.applicationContext().getColor(R.color.colorOwes) )
            }
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

    interface OnItemClickListener {
        fun onItemClick(item: Calculation)
    }
}