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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.DebtCurrenciesRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.ExpensesRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.support.DateFormatter
import com.macgavrina.co_accounting.viewmodel.DebtsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.add_debt_fragment.*
import kotlinx.android.synthetic.main.debt_activity.*
import java.util.*

class DebtActivityMVVM : AppCompatActivity(), DebtCurrenciesRecyclerViewAdapter.OnCurrencyClickListener {

    private lateinit var viewModel: DebtsViewModel
    private var debtId: Int = -1

    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null

    lateinit var contactsIdToNameMap: MutableMap<String, Contact>
    lateinit var positionToContactIdMap: MutableMap<Int, Contact>
    lateinit var contactIdToPositionMap: MutableMap<Int, Int>
    lateinit var friendsList: Array<String?>
    private var senderId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debt_activity)
        setSupportActionBar(toolbar)

        hideProgress()
        hideClearButton()
        hideDeleteButton()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(DebtsViewModel::class.java)

        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                displayToast(res)
            }
        })

        //ToDo REFACT Использовать фрагмент вместо activity и this.viewLifecycleOwner вместо this для всех observe
        viewModel.getAllDebtsForCurrentTrip().observe(this, Observer<List<Debt>> {})

        val extras = intent.extras
        if (extras?.getInt("debtId") != null && extras.getInt("debtId") != -1) {
            debtId = extras.getInt("debtId")
        }

        if (viewModel.getDebtById(debtId) != null) {
            viewModel.getDebtById(debtId)!!
                    .observe(this,
                            Observer<Debt> { debt ->
                                if (debt != null) {
                                    displayDebtData(debt)
                                } else {
                                    viewModel.createDebtDraft()
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe ({
                                                viewModel.getDebtById(debtId)!!
                                                        .observe(this,
                                                                Observer<Debt> { debt ->
                                                                    displayDebtData(debt)
                                                                })
                                            }, {error ->
                                                Log.d("Error creating debt draft, $error")
                                            })
                                }
                            })
        }

        val adapter = ExpensesRecyclerViewAdapter()
        add_debt_fragment_reciever_recyclerview.adapter = adapter
        add_debt_fragment_reciever_recyclerview.layoutManager = LinearLayoutManager(MainApplication.applicationContext())

        val currenciesAdapter = DebtCurrenciesRecyclerViewAdapter(this)
        add_debt_fragment_currencies_list.adapter = currenciesAdapter
        add_debt_fragment_currencies_list.layoutManager = LinearLayoutManager(MainApplication.applicationContext(), LinearLayoutManager.HORIZONTAL, true)

        viewModel.getAllExpensesForDebt(debtId).observe(this,
                Observer<List<Expense>> { expensesList ->
                    adapter.setExpenses(expensesList)

                    if (expensesList.isEmpty()) {
                        add_debt_fragment_empty_list_layout.visibility = View.VISIBLE
                    } else {
                        add_debt_fragment_empty_list_layout.visibility = View.INVISIBLE
                    }
                })

        viewModel.getAllActiveContactsForCurrentTrip().observe(this,
                Observer<List<Contact>> { contactsList ->
                    displayContactsList(contactsList)
                })

        viewModel.getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip().observe(this,
                Observer<List<Currency>> { currenciesList ->
                    val debt = viewModel.getCurrentDebt()
                    if (debt != null && debt.currencyId != -1) {
                        val currenciesListWithSavedForDebtMarker = mutableListOf<Currency>()
                        currenciesList.forEach { currency ->
                            currency.isActiveForCurrentTrip = currency.uid == debt.currencyId
                            currenciesListWithSavedForDebtMarker.add(currency)
                        }
                        currenciesAdapter.setCurrencies(currenciesListWithSavedForDebtMarker)
                    } else {
                        currenciesAdapter.setCurrencies(currenciesList)
                    }
                })

        add_debt_fragment_add_receiver_tv.setOnClickListener { view ->
            if (viewModel.getCurrentDebt() != null) {
                displayExpenseActivity(viewModel.getCurrentDebt()!!.uid, null)
            }
        }

        debt_fragment_delete_fab.setOnClickListener { view ->
            Log.d("Delete button is pressed, debt = ${viewModel.getCurrentDebt()}")
            if (viewModel.getCurrentDebt() != null) {
                viewModel.deleteDebt(viewModel.getCurrentDebt()!!)
                finishSelf()
            }
        }

        debt_fragment_clear_fab.setOnClickListener { view ->
            viewModel.clearDebtDraft()
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

        if (::friendsList.isInitialized && senderId != null) {

            if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[senderId!!]!=null) {
                setSender(contactIdToPositionMap[senderId!!]!!)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        senderId = positionToContactIdMap[getSender()]?.uid
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {
                doneButtonIsPressed()
                return true
            }
        }

        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        saveDebtDraft()
        super.onBackPressed()
    }

    override fun onCurrencyClick(selectedCurrencyId: Int) {
        viewModel.onCurrencyClick(selectedCurrencyId)
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


    private fun getSender(): Int {
        return add_debt_fragment_sender_spinner.selectedItemPosition
    }

    private fun getAmount(): String {
        return add_debt_fragment_amount_et.text.toString()
    }

    private fun getTime(): String {
        return add_debt_fragment_time_et.text.toString()
    }

    private fun getDate(): String {
        return add_debt_fragment_date_et.text.toString()
    }

    private fun getComment(): String {
        return add_debt_fragment_comment_et.text.toString()
    }

    private fun showProgress() {
        add_debt_fragment_progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        add_debt_fragment_progress_bar.visibility = View.INVISIBLE
    }

    private fun displayDebtData(debt: Debt) {
        Log.d("Displaying debt = $debt")
        if (senderId == null) {
            if (!debt.senderId.isNullOrEmpty() && ::friendsList.isInitialized) {
                Log.d("setSender")

                if (::contactIdToPositionMap.isInitialized && debt != null && contactIdToPositionMap?.isNotEmpty() && contactIdToPositionMap[debt.senderId?.toInt()]!=null) {
                    //setSender(contactIdToPositionMap[debt.senderId?.toInt()]!!)
                }
            }

            if (debt.senderId.isNullOrEmpty() && ::friendsList.isInitialized) {
                setSender(0)
            }
        }

        if (debt.spentAmount != null) {
            setAmount(debt.spentAmount!!)
        }

        if (debt.datetime != null) {
            setDate(DateFormatter().formatDateFromTimestamp(debt.datetime!!.toLong()))
            setTime(DateFormatter().formatTimeFromTimestamp(debt.datetime!!.toLong()))
        }

        if (debt.comment != null) {
            setComment(debt.comment!!)
        }

        if (debtId == -1) {
            hideDeleteButton()
            showClearButton()
        } else {
            hideClearButton()
            showDeleteButton()
        }
    }

    private fun setAmount(amount: String) {

        add_debt_fragment_amount_et.setText(amount)
    }

    private fun setDate(date: String) {

        add_debt_fragment_date_et.setText(date)
    }

    private fun setTime(time: String) {

        add_debt_fragment_time_et.setText(time)
    }

    private fun setComment(comment: String) {

        add_debt_fragment_comment_et.setText(comment)
    }


    private fun setSender(position: Int) {

        senderId = position
        add_debt_fragment_sender_spinner.setSelection(position)
    }

    private fun hideDeleteButton() {
        debt_fragment_delete_fab.hide()
    }

    private fun showDeleteButton() {
        debt_fragment_delete_fab.show()
    }

    private fun hideClearButton() {
        debt_fragment_clear_fab.hide()
    }

    private fun showClearButton() {
        debt_fragment_clear_fab.show()
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

    private fun showAlertAndGoToContacts(alertText: String) {
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

    private fun doneButtonIsPressed() {
        Log.d("Done button is pressed, currentDebt = ${viewModel.getCurrentDebt()}")
        hideKeyboard()

        var debt = viewModel.getCurrentDebt()
        if (debt == null) {
            debt = Debt()
        }
        debt.senderId = positionToContactIdMap[getSender()]?.uid.toString()
        debt.spentAmount= getAmount()

        if (getDate() != null) {

            if (getTime() == null) {
                val formattedDate = DateFormatter().getTimestampFromFormattedDate(getDate())
                if (formattedDate != null) {
                    debt.datetime = formattedDate.toString()
                }
            } else {
                val formattedDateTime = DateFormatter().getTimestampFromFormattedDateTime(
                        "${getDate()} ${getTime()}")
                if (formattedDateTime != null) {
                    debt.datetime = formattedDateTime.toString()
                }
            }
        }

        debt.comment = getComment()
        debt.status = "active"

        viewModel.updateDebtInDB(debt)
        finishSelf()
    }

    private fun displayExpenseActivity(debtId: Int, expenseId: Int?) {

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

    private fun saveDebtDraft() {
        if (viewModel.getCurrentDebt() == null || viewModel.getCurrentDebt()!!.status != "draft") return

        Log.d("handle back button pressed - save debt draft")

        val debt = viewModel.getCurrentDebt() ?: return

        debt.senderId = positionToContactIdMap[getSender()]?.uid.toString()
        debt.spentAmount= getAmount()

        if (getDate() != null) {

            if (getTime() == null) {
                val formattedDate = DateFormatter().getTimestampFromFormattedDate(getDate()!!)
                if (formattedDate != null) {
                    debt.datetime = formattedDate.toString()
                }
            } else {
                val formattedDateTime = DateFormatter().getTimestampFromFormattedDateTime(
                        "${getDate()} ${getTime()}")
                if (formattedDateTime != null) {
                    debt.datetime = formattedDateTime.toString()
                }
            }
        }

        debt.comment = getComment()

        viewModel.updateDebtInDB(debt)
    }

    private fun displayContactsList(contactsList: List<Contact>) {

        if (contactsList.isEmpty()) {
            showAlertAndGoToContacts("Please add at least one contact first")
        }

        friendsList = arrayOfNulls<String>(contactsList.size)
        contactsIdToNameMap = mutableMapOf()
        positionToContactIdMap = mutableMapOf()
        contactIdToPositionMap = mutableMapOf()
        var i = 0

        contactsList.forEach { contact ->
            friendsList[i] = contact.alias.toString()

            contactsIdToNameMap[contact.uid.toString()] = contact
            positionToContactIdMap[i] = contact
            contactIdToPositionMap[contact.uid] = i
            i = i + 1
        }


        setupSenderSpinner(friendsList)

        if (senderId == null) {

            val debt = viewModel.getCurrentDebt()
            if (debt?.senderId != null && debt.senderId!!.isNotEmpty() && debt.senderId != "null") {

                if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[debt.senderId?.toInt()] != null) {
                    setSender(contactIdToPositionMap[debt.senderId?.toInt()]!!)
                }
            }
        }
    }

    private fun setupSenderSpinner(contactsList: Array<String?>) {

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

}