package com.macgavrina.co_accounting.view

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.ActiveCurrenciesRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.ContactsRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversWithOnClickRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversWithOnClickRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.support.DateFormatter
import com.macgavrina.co_accounting.viewmodel.TripViewModel
import com.macgavrina.co_accounting.viewmodel.TripsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_trip.*
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.*
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.simple_expenses_list_selected_members_lv
import kotlinx.android.synthetic.main.contacts_fragment.*
import kotlinx.android.synthetic.main.simple_expenses_list.*
import kotlinx.android.synthetic.main.trip_fragment.*

class TripActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel
    private var startDatePickerDialog: DatePickerDialog? = null
    private var endDatePickerDialog: DatePickerDialog? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(TripViewModel::class.java)

        setupObserversForViewModel()

        setupOnChangeListeners()

        val extras = intent.extras
        val tripId = extras?.getInt("tripId")?: -1

        viewModel.tripIdIsReceivedFromActivity(tripId)
    }

    private fun setupObserversForViewModel() {
        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                displayToast(res)
            }
        })

//        viewModel.snackbarMessage.observe(this, Observer { text ->
//            Log.d("snackbar text is changed, observer reacts!")
//                val snackBar = Snackbar.make(trips_fragment_const_layout, "Trip is deleted", Snackbar.LENGTH_LONG)
//                snackBar.setAction("Undo") {
//                    Log.d("snackBar: undo action is pressed")
//                    snackBar.dismiss()
//                    viewModel.restoreLastDeletedTrip()
//                }
//                snackBar.show()
//        })

        viewModel.getCurrentTrip().observe(this,
                Observer<Trip> { trip ->
                    displayTrip(trip)
                })

        val adapter = ActiveCurrenciesRecyclerViewAdapter()
        trip_fragment_used_currencies_list.adapter = adapter
        trip_fragment_used_currencies_list.layoutManager = LinearLayoutManager(MainApplication.applicationContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.getCurrencies()?.observe(this,
                Observer<List<Currency>> { currencyList ->

                    adapter.setCurrencies(currencyList)

                    if (currencyList.isNotEmpty()) {
                        trip_fragment_empty_currencies_list.visibility = View.INVISIBLE
                    } else {
                        trip_fragment_empty_currencies_list.visibility = View.VISIBLE
                    }
                })

//        val contactsAdapter = ContactsRecyclerViewAdapter()
//        trip_fragment_contacts_list.adapter = contactsAdapter
//        trip_fragment_contacts_list.layoutManager = LinearLayoutManager(MainApplication.applicationContext())
//
//        viewModel.getAllContactsForCurrentTrip().observe(this,
//                Observer<List<Contact>> { contactsList ->
//                    contactsAdapter.setContacts(contactsList)
//
//                    Log.d("contactsList size = ${contactsList.size}")
////                    if (contactsList.isNotEmpty()) {
////                        contacts_fragment_empty_list_layout.visibility = View.INVISIBLE
////                    } else {
////                        contacts_fragment_empty_list_layout.visibility = View.VISIBLE
////                    }
//                })

        val viewManagerForSelected = LinearLayoutManager(MainApplication.applicationContext())
        trip_fragment_list_selected_members_lv.layoutManager = viewManagerForSelected

        viewModel.getSelectedContactsForCurrentTrip().observe(this,
                Observer {contactsList ->
                    trip_fragment_list_selected_members_lv.adapter = NotSelectedReceiversWithOnClickRecyclerViewAdapter(contactsList,
                            object: NotSelectedReceiversWithOnClickRecyclerViewAdapter.OnNotSelectedContactClickListener {
                                override fun onNotSelectedContactClick(selectedContact: Contact) {
                                    viewModel.onSelectedContactClick(selectedContact)
                                }

                            })
                    if (contactsList == null || contactsList.isEmpty()) {
                        trip_fragment_contacts_list_empty_list_layout.visibility = View.VISIBLE
                    } else {
                        trip_fragment_contacts_list_empty_list_layout.visibility = View.INVISIBLE
                    }
                })

        val viewManagerForNotSelected = LinearLayoutManager(MainApplication.applicationContext())
        trip_fragment_list_notselected_members_lv.layoutManager = viewManagerForNotSelected

        viewModel.getNotSelectedContactsForCurrentTrip().observe(this,
                Observer {contactsList ->
                    trip_fragment_list_notselected_members_lv.adapter = NotSelectedReceiversWithOnClickRecyclerViewAdapter(contactsList, object: NotSelectedReceiversWithOnClickRecyclerViewAdapter.OnNotSelectedContactClickListener {
                        override fun onNotSelectedContactClick(selectedContact: Contact) {
                            viewModel.onNotSelectedContactClick(selectedContact)
                        }

                    })
                })
    }

    private fun setupOnChangeListeners() {
        trip_fragment_edit_currencies_list_text.setOnClickListener {
            Log.d("onClick trip_fragment_edit_currencies_list_text")
            startCurrencyActivity(viewModel.getCurrentTrip().value?.uid ?: -1)
        }

        trip_fragment_delete_fab.setOnClickListener { view ->
            if (viewModel.isTripCanBeDeleted()) {
                showAlertBeforeDelete("Delete trip?")
            } else {
                displayAlertDialog("The only one trip can't be deleted")
            }
        }

        trip_fragment_startdate_et.setOnClickListener {
            displayStartDatePickerDialog()
        }

        trip_fragment_enddate_et.setOnClickListener {
            displayEndDatePickerDialog()
        }

        trip_fragment_title_et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.tripTitleIsChanged(p0.toString())
            }
        })

        trip_fragment_startdate_et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.startdateIsChanged(p0.toString())
            }
        })

        trip_fragment_enddate_et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.enddateIsChanged(p0.toString())
            }
        })

    }

    private fun displayTrip(trip: Trip) {
        trip_fragment_title_et.setText(trip.title)

        if (trip.startdate != 0L) {
            trip_fragment_startdate_et.setText(DateFormatter().formatDateFromTimestamp(trip.startdate!!))
        }

        if (trip.enddate != 0L) {
            trip_fragment_enddate_et.setText(DateFormatter().formatDateFromTimestamp(trip.enddate!!))
        }

        if (trip.status == "draft") {
            hideDeleteButton()
        } else {
            showDeleteButton()
        }

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

                    viewModel.saveTrip()
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

    private fun getEndDate(): Long? {
        return DateFormatter().getTimestampFromFormattedDate(trip_fragment_enddate_et.text.toString())
    }

    private fun getStartDate(): Long? {
        return DateFormatter().getTimestampFromFormattedDate(trip_fragment_startdate_et.text.toString())
    }

    private fun getTripTitle(): String {
        return trip_fragment_title_et.text.toString()
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
        trip_fragment_delete_fab.visibility = View.INVISIBLE
    }

    private fun showDeleteButton() {
        trip_fragment_delete_fab.visibility = View.VISIBLE
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

    private fun showAlertBeforeDelete(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
            alertDialogBuilder.setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTrip()
                finishSelf()
            }
            alertDialogBuilder.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                alertDialog?.dismiss()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun startCurrencyActivity(tripId: Int) {
        val intent = Intent()
        intent.action = "com.macgavrina.indebt.CURRENCY"
        intent.putExtra("tripId", tripId)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("onActivityResult, requestCode = $requestCode, resultCode = $requestCode")
        if (requestCode == 1) {
            viewModel.returnFromCurrenciesActivity()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}