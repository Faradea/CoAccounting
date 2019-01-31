package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.TripsRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.TripsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.presenters.TripsPresenter
import com.macgavrina.co_accounting.room.Trip
import kotlinx.android.synthetic.main.trips_fragment.*

class TripsFragment: Fragment(), TripsContract.View {

    lateinit var presenter: TripsPresenter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var tripsList: MutableList<Trip> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = TripsPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.trips_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        trips_fragment_add_fab.setOnClickListener { view ->
            presenter.addTripButtonIsPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        viewManager = LinearLayoutManager(MainApplication.applicationContext())
        trips_fragment_recyclerview.adapter = TripsRecyclerViewAdapter(tripsList)
        trips_fragment_recyclerview.layoutManager = viewManager
        presenter.viewIsReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun displayRevertChangesAction() {
    }

    override fun initializeList(inputTripsList: List<Trip>) {

        tripsList.clear()
        tripsList.addAll(inputTripsList)

        if (tripsList.isNotEmpty()) {
            trips_fragment_empty_list_layout.visibility = View.INVISIBLE
            Log.d(trips_fragment_recyclerview.adapter.toString())
            trips_fragment_recyclerview.adapter?.notifyDataSetChanged()
            //trips_fragment_recyclerview.adapter = TripsRecyclerViewAdapter(tripsList)
        } else {
            trips_fragment_empty_list_layout.visibility = View.VISIBLE
        }
    }

    override fun updateList() {
    }

    override fun showProgress() {
        trips_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        trips_fragment_progress_bar.visibility = View.INVISIBLE
    }

    override fun displayOnDeleteTripSnackBar() {
        val snackBar = Snackbar.make(trips_fragment_const_layout, "Trip is deleted", Snackbar.LENGTH_LONG)
        snackBar!!.setAction("Undo") {
            Log.d("snackBar: undo action is pressed")
            snackBar?.dismiss()
            presenter.undoDeleteTripButtonIsPressed()
        }
        snackBar?.show()
    }

}