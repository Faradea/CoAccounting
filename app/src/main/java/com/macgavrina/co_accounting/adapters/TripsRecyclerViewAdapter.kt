package com.macgavrina.co_accounting.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.DateFormatter
import kotlinx.android.synthetic.main.trips_list_item.view.*

class TripsRecyclerViewAdapter (tripsList: List<Trip>?) :
        RecyclerView.Adapter<TripsRecyclerViewAdapter.ViewHolder>() {

    private val mItems: List<Trip>? = tripsList

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val titleTV = view.trips_list_item_title_tv
        val datesTV = view.trips_list_item_dates_tv
        val isCurrentSwitch = view.trips_list_item_switch

        private var mItem: Trip? = null

        init {
            view.setOnClickListener{
                MainApplication.bus.send(Events.OnClickTripList(mItem?.uid.toString()))
            }

            isCurrentSwitch.setOnClickListener {
                MainApplication.bus.send(Events.OnClickSwitchTripList(mItem?.uid.toString(), isCurrentSwitch.isChecked))
            }
        }

        fun setItem(item: Trip) {
            mItem = item
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TripsRecyclerViewAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // create a new view
        val view = layoutInflater.inflate(R.layout.trips_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        val item = mItems?.get(position) ?: return

        holder.titleTV.text = item.title

        var datesText = ""

        if (item.startdate != null) {
            datesText = DateFormatter().formatDateFromTimestamp(item.startdate!!)

            if (item.enddate != null) {
                datesText = "$datesText - ${DateFormatter().formatDateFromTimestamp(item.enddate!!)}"
            }
        }

        holder.datesTV.text = datesText
        holder.isCurrentSwitch.isChecked = item.isCurrent

        holder.setItem(mItems[position])
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