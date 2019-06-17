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
import com.macgavrina.co_accounting.adapters.DebtsRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.viewmodel.DebtsViewModel
import kotlinx.android.synthetic.main.debts_fragment.*

class DebtsFragmentMVVM: Fragment() {

    private lateinit var viewModel: DebtsViewModel
    //private var tripsList: MutableList<Trip> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.debts_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(DebtsViewModel::class.java)

        hideProgress()

        debts_fragment_add_fab.setOnClickListener { view ->
            viewModel.addDebtButtonIsPressed()
        }



        val adapter = DebtsRecyclerViewAdapter()
        debts_fragment_recyclerview.adapter = adapter
        debts_fragment_recyclerview.layoutManager = LinearLayoutManager(MainApplication.applicationContext())

        viewModel.getAllDebtsForCurrentTrip().observe(this.viewLifecycleOwner,
                Observer<List<Debt>> { debtsList ->
                    adapter.setDebts(debtsList)

                    if (debtsList.isNotEmpty()) {
                        debts_fragment_empty_list_layout.visibility = View.INVISIBLE
                    } else {
                        debts_fragment_empty_list_layout.visibility = View.VISIBLE
                    }
                })


        viewModel.toastMessage.observe(this.viewLifecycleOwner, Observer { text ->
            Log.d("Toast text is changed to: $text, observer reacts!")
            if (!text.isNullOrEmpty()) {
                displayToast(text)
            }
        })

        viewModel.getAllTrips().observe(this,
                Observer<List<Trip>> { tripsList ->
                    Log.d("Trips list is received from DB, size = ${tripsList.size}, value = $tripsList")
                    setTripsList(tripsList)
                })

        debts_fragment_trip_autocompletetv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                viewModel.tripIsChanged(s.toString())
            }
        })

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
        debts_fragment_progress_bar.visibility = View.INVISIBLE
    }

    private fun setTripsList(tripsList: List<Trip>) {

        debts_fragment_trip_autocompletetv.setOnClickListener {
            debts_fragment_trip_autocompletetv.forceFiltering()
        }

        val tripsArray = arrayOfNulls<String>(tripsList.size)

        var i = 0
        tripsList.forEach { trip ->
            tripsArray[i] = trip.title
            i += 1
            if (trip.isCurrent) {
                debts_fragment_trip_autocompletetv.setText(trip.title)
            }
        }

        val adapter = ArrayAdapter<String>(
                MainApplication.applicationContext(),
                R.layout.dropdown_menu_popup_item,
                tripsArray
        )

        debts_fragment_trip_autocompletetv.setAdapter(adapter)
    }
}