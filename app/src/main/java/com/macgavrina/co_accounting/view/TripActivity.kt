package com.macgavrina.co_accounting.view

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.ActiveCurrenciesRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.DateFormatter
import com.macgavrina.co_accounting.viewmodel.TripsViewModel
import io.reactivex.MaybeObserver
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_trip.*
import kotlinx.android.synthetic.main.trip_fragment.*
import kotlinx.android.synthetic.main.trips_fragment.*

class TripActivity : AppCompatActivity() {

    private lateinit var tripsViewModel: TripsViewModel
    private var startDatePickerDialog: DatePickerDialog? = null
    private var endDatePickerDialog: DatePickerDialog? = null
    private var tripId: Int? = null
    private var trip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tripsViewModel = ViewModelProviders.of(this).get(TripsViewModel::class.java)

        tripsViewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                displayToast(res)
            }
        })

//        tripsViewModel.snackbarMessage.observe(this, Observer { text ->
//            Log.d("snackbar text is changed, observer reacts!")
//                val snackBar = Snackbar.make(trips_fragment_const_layout, "Trip is deleted", Snackbar.LENGTH_LONG)
//                snackBar.setAction("Undo") {
//                    Log.d("snackBar: undo action is pressed")
//                    snackBar.dismiss()
//                    tripsViewModel.restoreLastDeletedTrip()
//                }
//                snackBar.show()
//        })

        //ToDo REFACT Использовать фрагмент вместо activity и this.viewLifecycleOwner вместо this для всех observe
        tripsViewModel.getAll().observe(this, Observer<List<Trip>> {})

        val adapter = ActiveCurrenciesRecyclerViewAdapter()
        trip_fragment_used_currencies_list.adapter = adapter
        trip_fragment_used_currencies_list.layoutManager = LinearLayoutManager(MainApplication.applicationContext(), LinearLayoutManager.HORIZONTAL, false)

        val extras = intent.extras
        if (extras?.getInt("tripId") != -1) {
            tripId = extras?.getInt("tripId")
            tripsViewModel.getTripById(tripId!!)
                    .observe(this,
                            Observer<Trip> { trip ->
                                this.trip = trip
                                trip_fragment_title_et.setText(trip.title)

                                if (trip.startdate != null) {
                                    trip_fragment_startdate_et.setText(DateFormatter().formatDateFromTimestamp(trip.startdate!!))
                                }

                                if (trip.enddate != null) {
                                    trip_fragment_enddate_et.setText(DateFormatter().formatDateFromTimestamp(trip.enddate!!))
                                }

                                trip_fragment_switch.isChecked = trip.isCurrent
                            })

            tripsViewModel.getCurrenciesForTrip(tripId!!)
                    .observe(this,
                            Observer<List<Currency>> { currencyList ->
                                adapter.setCurrencies(currencyList)

                                if (currencyList.isNotEmpty()) {
                                    trip_fragment_empty_currencies_list.visibility = View.INVISIBLE
                                } else {
                                    trip_fragment_empty_currencies_list.visibility = View.VISIBLE
                                }
                            })

        } else {
            hideDeleteButton()
        }

        trip_fragment_edit_currencies_list_text.setOnClickListener {
            Log.d("onClick trip_fragment_edit_currencies_list_text")
            if (tripId != null && tripId != -1) {
                startCurrencyActivity(tripId!!)
            }
        }

        trip_fragment_delete_fab.setOnClickListener { view ->
            if (trip != null) {
                tripsViewModel.deleteTrip(trip!!)
            }
            finishSelf()
        }

        trip_fragment_startdate_et.setOnClickListener {
            displayStartDatePickerDialog()
        }

        trip_fragment_enddate_et.setOnClickListener {
            displayEndDatePickerDialog()
        }

//        contact_fragment_email_et.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(s: Editable) {}
//
//            override fun beforeTextChanged(s: CharSequence, start: Int,
//                                           count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int,
//                                       before: Int, count: Int) {
//                if (s.isEmpty()) {
//                    contact_fragment_email_til.error = "Enter email"
//                } else {
//                    contact_fragment_email_til.error = null
//                }
//            }
//        })

    }

    //For back button in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {

                hideKeyboard()

                var duration = 0L

                if (getEndDate() != null && getStartDate() != null) {
                    duration = getEndDate()!! - getStartDate()!!
                }

                if (!getTripTitle().isNullOrEmpty() && duration >= 0 ) {
                    val trip = Trip()
                    trip.title = getTripTitle()
                    trip.startdate = getStartDate()
                    trip.enddate = getEndDate()
                    trip.isCurrent = getSwitchStatus()
                    trip.status = "active"

                    if (tripId == null) {
                        tripsViewModel.insertTrip(trip)
                    } else {
                        trip.uid = tripId!!.toInt()
                        tripsViewModel.updateTrip(trip)
                    }
                    finishSelf()
                    return true
                } else {
                    if (getTripTitle().isNullOrEmpty()) {
                        trip_fragment_title_til.error = "Enter title"
                    }
                    if (duration < 0) {
                        trip_fragment_enddate_et.error = "Can't be less than start date"
                    }
                    return true
                }

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
        tripsViewModel.viewIsDestroyed()
    }

    private fun getEndDate(): Long? {
        return DateFormatter().getTimestampFromFormattedDate(trip_fragment_enddate_et.text.toString())
    }

    private fun getStartDate(): Long? {
        return DateFormatter().getTimestampFromFormattedDate(trip_fragment_startdate_et.text.toString())
    }

    private fun getTripTitle(): String {
        return trip_fragment_title_et.text.toString()
    }

    private fun getSwitchStatus(): Boolean {
        return trip_fragment_switch.isChecked
    }

    private fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    private fun displayToast(text: String) {
        Log.d("Display toast with text = $text")
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun finishSelf() {
        onBackPressed()
    }


    private fun showProgress() {
        trip_fragment_progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        trip_fragment_progressBar.visibility = View.INVISIBLE
    }

    private fun hideDeleteButton() {
        trip_fragment_delete_fab.hide()
    }

    private fun displayAlertDialog(alertText: String) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(alertText)
                .setTitle("Alert")
        alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
            finishSelf()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun displayStartDatePickerDialog() {

        if (startDatePickerDialog != null || endDatePickerDialog != null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            startDatePickerDialog = DatePickerDialog(this)

            val calendar = Calendar.getInstance()

            startDatePickerDialog?.setOnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                trip_fragment_startdate_et.setText(DateFormatter().formatDateFromTimestamp(calendar.timeInMillis))
                startDatePickerDialog = null
            }

            startDatePickerDialog?.setOnCancelListener{
                startDatePickerDialog = null
            }

            startDatePickerDialog?.show()

            if (getStartDate() != null) {

                calendar.timeInMillis = getStartDate()!!

                val mYear = calendar.get(Calendar.YEAR)
                val mMonth = calendar.get(Calendar.MONTH)
                val mDay = calendar.get(Calendar.DAY_OF_MONTH)

                startDatePickerDialog?.updateDate(mYear, mMonth, mDay)

            }

        } else {
            TODO("VERSION.SDK_INT < N")
        }
    }


    private fun displayEndDatePickerDialog() {
        if (startDatePickerDialog != null || endDatePickerDialog != null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            endDatePickerDialog = DatePickerDialog(this)

            val calendar = Calendar.getInstance()

            endDatePickerDialog?.setOnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                trip_fragment_enddate_et.setText(DateFormatter().formatDateFromTimestamp(calendar.timeInMillis))
                trip_fragment_enddate_til.error = null
                endDatePickerDialog = null
            }

            endDatePickerDialog?.setOnCancelListener{
                endDatePickerDialog = null
            }

            endDatePickerDialog?.show()

            if (getEndDate() != null) {

                calendar.timeInMillis = getEndDate()!!

                val mYear = calendar.get(Calendar.YEAR)
                val mMonth = calendar.get(Calendar.MONTH)
                val mDay = calendar.get(Calendar.DAY_OF_MONTH)

                endDatePickerDialog?.updateDate(mYear, mMonth, mDay)

            }

        } else {
            TODO("VERSION.SDK_INT < N")
        }
    }

    private fun startCurrencyActivity(tripId: Int) {
        val intent = Intent()
        intent.action = "com.macgavrina.indebt.CURRENCY"
        intent.putExtra("tripId", tripId)
        startActivity(intent)
    }

}