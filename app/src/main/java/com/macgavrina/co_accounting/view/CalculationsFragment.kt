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
import com.macgavrina.co_accounting.adapters.CalculationsRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Calculation
import com.macgavrina.co_accounting.viewmodel.CalculationsViewModel
import kotlinx.android.synthetic.main.calculations_fragment.*

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
                    adapter.setCalculations(calculationsList)

                    if (calculationsList.size == 0) {
                        calculations_fragment_empty_list_layout.visibility = View.VISIBLE
                    } else {
                        calculations_fragment_empty_list_layout.visibility = View.INVISIBLE
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
}