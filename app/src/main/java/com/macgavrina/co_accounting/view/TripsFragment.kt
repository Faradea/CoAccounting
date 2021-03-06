package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.TripsRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.viewmodel.TripsViewModel
import kotlinx.android.synthetic.main.trips_fragment.*


class TripsFragment: Fragment() {

    private lateinit var tripsViewModel: TripsViewModel
    //private var tripsList: MutableList<Trip> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.trips_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("onActivityCreated, getting TripsViewModel instance")

        trips_fragment_add_fab.setOnClickListener { view ->
            tripsViewModel.addTripButtonIsPressed()
        }

        tripsViewModel = ViewModelProviders.of(this).get(TripsViewModel::class.java)
        val adapter = TripsRecyclerViewAdapter(tripsViewModel)
        trips_fragment_recyclerview.adapter = adapter
        trips_fragment_recyclerview.layoutManager = LinearLayoutManager(MainApplication.applicationContext())
        tripsViewModel.getAll().observe(this.viewLifecycleOwner,
                Observer<List<Trip>> { tripsList ->
                    adapter.setTrips(tripsList!!)

                    if (tripsList.isNotEmpty()) {

                        printAllTrips(tripsList)
                        trips_fragment_empty_list_layout.visibility = View.INVISIBLE
                        Log.d(trips_fragment_recyclerview.adapter.toString())
                        //trips_fragment_recyclerview.adapter?.notifyDataSetChanged()
                        //trips_fragment_recyclerview.adapter = TripsRecyclerViewAdapter(tripsList)
                    } else {
                        trips_fragment_empty_list_layout.visibility = View.VISIBLE
                    }

                })

        if (!tripsViewModel.toastMessage.hasObservers()) {
            Log.d("Register observer for toastMessage")
            tripsViewModel.toastMessage.removeObservers(this)
            tripsViewModel.toastMessage.observe(this.viewLifecycleOwner, Observer { text ->
                Log.d("Toast text is changed to: $text, observer reacts!")
                if (!text.isNullOrEmpty()) {
                    displayToast(text)
                }
            })
        }

//        tripsViewModel.snackbarMessage.observe(this, Observer { text ->
//            Log.d("snackbar text is changed, observer reacts!")
//            val snackBar = Snackbar.make(trips_fragment_const_layout, "Trip is deleted", Snackbar.LENGTH_LONG)
//            snackBar.setAction("Undo") {
//                Log.d("snackBar: undo action is pressed")
//                snackBar.dismiss()
//                tripsViewModel.restoreLastDeletedTrip()
//            }
//            snackBar.show()
//        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        tripsViewModel.viewIsDestroyed()
    }

    private fun displayToast(text: String) {
        Log.d("Display toast with text = $text")
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun showProgress() {
        //trips_fragment_progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        //trips_fragment_progress_bar.visibility = View.INVISIBLE
    }

    private fun printAllTrips(tripsList: List<Trip>) {
        Log.d("Printing all trips...")
        tripsList.forEach { trip ->
            Log.d(trip.toString())
        }
    }
}
