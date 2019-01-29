package com.macgavrina.co_accounting.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.ExpensesRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.DebtActivityContract
import com.macgavrina.co_accounting.presenters.DebtActivityPresenter
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.support.DateFormatter
import kotlinx.android.synthetic.main.add_debt_fragment.*
import kotlinx.android.synthetic.main.debt_activity.*
import java.util.*


class DebtActivity : AppCompatActivity(), DebtActivityContract.View {

    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null
    private var senderId: Int? = null
    lateinit var presenter: DebtActivityPresenter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debt_activity)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = DebtActivityPresenter()
        presenter.attachView(this)
        presenter.viewIsCreated()


        val extras = intent.extras
        val debtId = extras?.getInt("debtId")

        //val debtId = savedInstanceState?.getInt(AddReceiverInAddDebtFragment.DEBT_ID_KEY)
        if (debtId == -1) {
            presenter.debtIdIsReceiverFromMainActivity(null)
        } else {
            presenter.debtIdIsReceiverFromMainActivity(debtId)
        }

        add_debt_fragment_add_receiver_tv.setOnClickListener { view ->
            presenter.addExpenseButtonIsPressed()
        }

        debt_fragment_delete_fab.setOnClickListener { view ->
            presenter.deleteButtonIsPressed()
        }

        debt_fragment_clear_fab.setOnClickListener { view ->
            presenter.clearButtonIsPressed()
        }

        add_debt_fragment_date_et.setOnClickListener { view ->
            displayDatePickerDialog()
        }

        add_debt_fragment_time_et.setOnClickListener { view ->
            displayTimePickerDialog()
        }

    }

    override fun onResume() {
        super.onResume()

        viewManager = LinearLayoutManager(MainApplication.applicationContext())

        presenter.viewIsReady()
    }

    override fun onPause() {
        super.onPause()
        presenter.viewIsPaused()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    //For back button in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {
                presenter.doneButtonIsPressed()
                return true
            }
        }

        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        presenter.saveDebtDraft()
        super.onBackPressed()
    }

    //For action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return true
    }

    private fun displayTimePickerDialog() {
        if (timePickerDialog != null) return

        var mcurrentTime = Calendar.getInstance()

        if (getTime() != null && getTime().isNotEmpty()) {
            mcurrentTime.timeInMillis = DateFormatter().
                    getTimestampFromFormattedDateTime("${getDate()} ${getTime()}")!!
        }

        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)

        timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener {
            timePicker, selectedHour, selectedMinute ->
            add_debt_fragment_time_et.setText("$selectedHour:$selectedMinute")
            timePickerDialog = null
        }, hour, minute, true)

        timePickerDialog?.setOnCancelListener {
            timePickerDialog = null
        }

        timePickerDialog?.show()
    }

    private fun displayDatePickerDialog() {

        if (datePickerDialog != null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            datePickerDialog = DatePickerDialog(this)

            val calendar = Calendar.getInstance()

            datePickerDialog?.setOnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                add_debt_fragment_date_et.setText(DateFormatter().formatDateFromTimestamp(calendar.timeInMillis))
                datePickerDialog = null
            }

            datePickerDialog?.setOnCancelListener{
                datePickerDialog = null
            }

            datePickerDialog?.show()

            if (getDate() != null && getDate().isNotEmpty()) {

                calendar.timeInMillis = DateFormatter().getTimestampFromFormattedDate(getDate())!!

                val mYear = calendar.get(Calendar.YEAR)
                val mMonth = calendar.get(Calendar.MONTH)
                val mDay = calendar.get(Calendar.DAY_OF_MONTH)

                datePickerDialog?.updateDate(mYear, mMonth, mDay)

            }

        } else {
            TODO("VERSION.SDK_INT < N")
        }
    }


    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun setSender(position: Int) {

        senderId = position
        add_debt_fragment_sender_spinner.setSelection(position)
    }

    override fun setAmount(amount: String) {

        add_debt_fragment_amount_et.setText(amount)
    }

    override fun setDate(date: String) {

        add_debt_fragment_date_et.setText(date)
    }

    override fun setTime(time: String) {

        add_debt_fragment_time_et.setText(time)
    }

    override fun setComment(comment: String) {

        add_debt_fragment_comment_et.setText(comment)
    }

    override fun getSender(): Int {
        return add_debt_fragment_sender_spinner.selectedItemPosition
    }

    override fun getAmount(): String {
        return add_debt_fragment_amount_et.text.toString()
    }

    override fun getTime(): String {
        return add_debt_fragment_time_et.text.toString()
    }

    override fun getDate(): String {
        return add_debt_fragment_date_et.text.toString()
    }

    override fun getComment(): String {
        return add_debt_fragment_comment_et.text.toString()
    }

    override fun showProgress() {
        add_debt_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        add_debt_fragment_progress_bar.visibility = View.INVISIBLE
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun setupSenderSpinner(contactsList: Array<String?>) {

        val adapter = ArrayAdapter<String>(
                MainApplication.applicationContext(),
                android.R.layout.simple_spinner_item,
                contactsList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        add_debt_fragment_sender_spinner.adapter = adapter

        if (senderId != null) {
            setSender(senderId!!)
        }
    }

    override fun initializeExpensesList(expenseList: List<Expense>?) {
        if (expenseList == null || expenseList.isEmpty()) {
            add_debt_fragment_empty_list_layout.visibility = View.VISIBLE
        } else {
            add_debt_fragment_empty_list_layout.visibility = View.INVISIBLE
            add_debt_fragment_reciever_recyclerview.adapter = ExpensesRecyclerViewAdapter(expenseList)
            add_debt_fragment_reciever_recyclerview.layoutManager = viewManager
        }
    }

    override fun finishSelf() {
        onBackPressed()
    }

    override fun displayExpenseActivity(debtId: Int, expenseId: Int?) {

        val intent = Intent()
        intent.action = "com.macgavrina.indebt.EXPENSE"
        intent.putExtra("debtId", debtId)
        if (expenseId != null) {
            intent.putExtra("expenseId", expenseId)
        } else {
            intent.putExtra("expenseId", -1)
        }

        startActivity(intent)
    }

    override fun showAlertAndGoToContacts(alertText: String) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
                    .setTitle("Alert")
            alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
                //ToDo NEW открывать вкладку contacts после этого
                finishSelf()
            }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

    override fun hideDeleteButton() {
        debt_fragment_delete_fab.hide()
    }

    override fun showDeleteButton() {
        debt_fragment_delete_fab.show()
    }

    override fun hideClearButton() {
        debt_fragment_clear_fab.hide()
    }

    override fun showClearButton() {
        debt_fragment_clear_fab.show()
    }
}