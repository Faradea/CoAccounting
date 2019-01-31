package com.macgavrina.co_accounting.view

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.TripContract
import com.macgavrina.co_accounting.presenters.TripPresenter
import com.macgavrina.co_accounting.support.DateFormatter
import kotlinx.android.synthetic.main.activity_trip.*
import kotlinx.android.synthetic.main.trip_fragment.*
import java.util.*

class TripActivity : AppCompatActivity(), TripContract.View {

    lateinit var presenter: TripPresenter
    private var startDatePickerDialog: DatePickerDialog? = null
    private var endDatePickerDialog: DatePickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = TripPresenter()
        presenter.attachView(this)
        presenter.viewIsCreated()

        trip_fragment_delete_fab.setOnClickListener { view ->
            presenter.deleteButtonIsPressed()
        }

        val extras = intent.extras
        val tripId = extras?.getInt("tripId")

        if (tripId == -1) {
            presenter.tripIdIsReceiverFromMainActivity(null)
        } else {
            presenter.tripIdIsReceiverFromMainActivity(tripId.toString())
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

    override fun onResume() {
        super.onResume()

        presenter.viewIsReady()
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    //For back button in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {

                var duration = 0L

                if (getEndDate() != null && getStartDate() != null) {
                    duration = getEndDate()!! - getStartDate()!!
                }

                if (!getTripTitle().isNullOrEmpty() && duration >= 0 ) {
                    presenter.doneButtonIsPressed()
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

    override fun displayTripData(title: String?, startDate: Long?, endDate: Long?, isCurrent: Boolean) {
        trip_fragment_title_et.setText(title)

        if (startDate != null) {
            trip_fragment_startdate_et.setText(DateFormatter().formatDateFromTimestamp(startDate))
        }

        if (endDate != null) {
            trip_fragment_enddate_et.setText(DateFormatter().formatDateFromTimestamp(endDate))
        }

        trip_fragment_switch.isChecked = isCurrent

    }

    override fun getEndDate(): Long? {
        return DateFormatter().getTimestampFromFormattedDate(trip_fragment_enddate_et.text.toString())
    }

    override fun getStartDate(): Long? {

        return DateFormatter().getTimestampFromFormattedDate(trip_fragment_startdate_et.text.toString())
    }

    override fun getTripTitle(): String {
        return trip_fragment_title_et.text.toString()
    }

    override fun getSwitchStatus(): Boolean {
        return trip_fragment_switch.isChecked
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun finishSelf() {
        onBackPressed()
    }


    override fun showProgress() {
        trip_fragment_progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        trip_fragment_progressBar.visibility = View.INVISIBLE
    }

    override fun hideDeleteButton() {
        trip_fragment_delete_fab.hide()
    }

    override fun displayAlertDialog(alertText: String) {
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


}