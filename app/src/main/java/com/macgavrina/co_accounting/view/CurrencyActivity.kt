package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.ContactsRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.CurrenciesRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.viewmodel.CurrenciesViewModel
import kotlinx.android.synthetic.main.activity_currency.*
import kotlinx.android.synthetic.main.contacts_fragment.*
import kotlinx.android.synthetic.main.currency_fragment.*

class CurrencyActivity : AppCompatActivity() {

    private lateinit var viewModel: CurrenciesViewModel
    private var tripId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = CurrenciesRecyclerViewAdapter()
        cirrency_fragment_currencies_list.adapter = adapter
        cirrency_fragment_currencies_list.layoutManager = LinearLayoutManager(MainApplication.applicationContext())

        viewModel = ViewModelProviders.of(this).get(CurrenciesViewModel::class.java)

        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                displayToast(res)
            }
        })

        //ToDo REFACT Использовать фрагмент вместо activity и this.viewLifecycleOwner вместо this для всех observe

        val extras = intent.extras
        if (extras?.getInt("tripId") != -1) {
            tripId = extras?.getInt("tripId")
        }



        if (tripId != null) {
            viewModel.getAllCurrenciesForTrip(tripId!!).observe(this,
                    Observer<List<Currency>> { currenciesList ->
                        adapter.setCurrencies(currenciesList)
                    })
        }




//            if (tripId != null && tripId != -1) {
//                viewModel.getAllCurrenciesForTrip(tripId!!).observe(this, Observer<List<Currency>> {
//                    .observe(this,
//                            Observer<Trip> { trip ->
//                                this.trip = trip
//                                trip_fragment_title_et.setText(trip.title)
//
//                                if (trip.startdate != null) {
//                                    trip_fragment_startdate_et.setText(DateFormatter().formatDateFromTimestamp(trip.startdate!!))
//                                }
//
//                                if (trip.enddate != null) {
//                                    trip_fragment_enddate_et.setText(DateFormatter().formatDateFromTimestamp(trip.enddate!!))
//                                }
//
//                                trip_fragment_switch.isChecked = trip.isCurrent
//                            })
//                })
//            }

    }

    //For back button in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {
                finishSelf()
            }
        }

        onBackPressed()
        return true
    }

    //For action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.viewIsDestroyed()
    }

    private fun displayToast(text: String) {
        Log.d("Display toast with text = $text")
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun finishSelf() {
        onBackPressed()
    }


    private fun showProgress() {
        //trip_fragment_progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        //trip_fragment_progressBar.visibility = View.INVISIBLE
    }
}