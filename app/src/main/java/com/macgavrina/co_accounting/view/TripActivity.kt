package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.TripContract
import com.macgavrina.co_accounting.presenters.TripPresenter
import kotlinx.android.synthetic.main.activity_trip.*
import kotlinx.android.synthetic.main.trip_fragment.*

class TripActivity : AppCompatActivity(), TripContract.View {

    lateinit var presenter: TripPresenter

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

                if (!getTripTitle().isNullOrEmpty()) {
                    presenter.doneButtonIsPressed()
                    return true
                } else {
                    if (getTripTitle().isNullOrEmpty()) {
                        trip_fragment_title_til.error = "Enter title"
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

    override fun displayTripData(title: String?, startDate: String, endDate: String, isCurrent: Boolean) {
        //TODO
    }

    override fun getEndDate(): String {
        return trip_fragment_enddate_et.text.toString()
    }

    override fun getStartDate(): String {
        return trip_fragment_startdate_et.text.toString()
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

    //ToDo Выводить предупреждение что все данные о поездке тоже буду удалены
//    override fun displayAlert(text: String, title: String) {
//        val builder = AlertDialog.Builder(this)
//        builder.setMessage(text)
//                .setTitle(title)
//                .setPositiveButton("ok") { _, _ -> }
//        val dialog = builder.create()
//        dialog.show()
//    }

    override fun hideDeleteButton() {
        trip_fragment_delete_fab.hide()
    }
}