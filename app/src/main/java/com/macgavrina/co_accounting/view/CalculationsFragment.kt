package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.CalculationsRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Calculation
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.viewmodel.CalculationsViewModel
import kotlinx.android.synthetic.main.calculations_fragment.*
import kotlinx.android.synthetic.main.debts_fragment.*

class CalculationsFragment: Fragment() {

    private lateinit var viewModel: CalculationsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.calculations_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(CalculationsViewModel::class.java)

        val adapter = CalculationsRecyclerViewAdapter()
        calculations_fragment_recycler_view.adapter = adapter
        calculations_fragment_recycler_view.layoutManager = LinearLayoutManager(MainApplication.applicationContext())

        viewModel.getAllCalculationsForCurrentTrip().observe(this.viewLifecycleOwner,
                Observer<List<Calculation>> { calculationsList ->

                    val filteredCalculationList = mutableListOf<Calculation>()
                    calculationsList.forEach { calculation ->
                        if (calculation.totalAmount != 0.0) {
                            filteredCalculationList.add(calculation)
                        }

                    }
                    adapter.setCalculations(filteredCalculationList)

                    if (filteredCalculationList.size == 0) {
                        calculations_fragment_empty_list_layout.visibility = View.VISIBLE
                    } else {
                        calculations_fragment_empty_list_layout.visibility = View.INVISIBLE
                    }
                })

        viewModel.getAllTrips().observe(this,
                Observer<List<Trip>> { tripsList ->
                    Log.d("Trips list is received from DB, size = ${tripsList.size}, value = $tripsList")
                    setTripsList(tripsList)
                })

        calculations_fragment_trip_autocompletetv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                viewModel.tripIsChanged(s.toString())
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.viewIsDestroyed()
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

    private fun setTripsList(tripsList: List<Trip>) {

        calculations_fragment_trip_autocompletetv.setOnClickListener {
            calculations_fragment_trip_autocompletetv.forceFiltering()
        }

        val tripsArray = arrayOfNulls<String>(tripsList.size)

        var i = 0
        tripsList.forEach { trip ->
            tripsArray[i] = trip.title
            i += 1
            if (trip.isCurrent) {
                calculations_fragment_trip_autocompletetv.setText(trip.title)
            }
        }

        val adapter = ArrayAdapter<String>(
                MainApplication.applicationContext(),
                R.layout.dropdown_menu_popup_item,
                tripsArray
        )

        calculations_fragment_trip_autocompletetv.setAdapter(adapter)
    }
}