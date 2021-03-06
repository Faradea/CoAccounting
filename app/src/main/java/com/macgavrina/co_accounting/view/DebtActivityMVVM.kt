package com.macgavrina.co_accounting.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.*
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.support.*
import com.macgavrina.co_accounting.viewmodel.DebtViewModel
import com.macgavrina.co_accounting.viewmodel.EXPENSE_ID_KEY
import kotlinx.android.synthetic.main.add_debt_fragment.*
import kotlinx.android.synthetic.main.debt_activity.*
import java.util.*

class DebtActivityMVVM : AppCompatActivity(), DebtCurrenciesRecyclerViewAdapter.OnCurrencyClickListener {

    private lateinit var viewModel: DebtViewModel
    private var debtId: Int = -1

    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null
    private var alertDialog: AlertDialog? = null

    private var expenseIdForSimpleMode: Int = -1
    private var extendedExpensesFragment: ExtendedExpensesFragment? = null
    private var simpleExpensesFragment: SimpleExpensesFragment? = null

    private var expensesListSize: Int = -1
    private var expensesList = mutableListOf<Expense>()

    private var allContactsForAllTripsCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debt_activity)
        setSupportActionBar(toolbar)

        hideProgress()
        hideClearButton()
        hideDeleteButton()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViewModelAndObservers()

        setOnClickListeners()
    }

    //For action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {
                if (add_debt_fragment_sender_autocompletetv.text.isNotEmpty()) {
                    doneButtonIsPressed()
                } else {
                    add_debt_fragment_sender_lil.error = "Select who paid"
                }
                return true
            }
        }

        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
        super.onBackPressed()
    }

    override fun onCurrencyClick(selectedCurrencyId: Int) {
        viewModel.onCurrencyClick(selectedCurrencyId)
    }

    private fun setupViewModelAndObservers() {
        viewModel = ViewModelProviders.of(this).get(DebtViewModel::class.java)

        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                displayToast(res)
            }
        })

        val extras = intent.extras
        if (extras?.getInt("debtId") != null && extras.getInt("debtId") != -1) {
            debtId = extras.getInt("debtId")
            viewModel.debtIdIsReceivedFromIntent(debtId)
        } else {
            viewModel.debtIdIsNotReceivedFromIntent()
        }


        viewModel.getCurrentDebt().observe(this,
                Observer<Debt> { debt ->
                    if (debt != null) {
                        displayDebtData(debt)
                    }
                })

        viewModel.getAllContactsCount().observe(this,
                Observer<Int> { allcontactsCount ->
                    this.allContactsForAllTripsCount = allcontactsCount
                })


        val currenciesAdapter = DebtCurrenciesRecyclerViewAdapter(this)
        add_debt_fragment_currencies_list.adapter = currenciesAdapter
        add_debt_fragment_currencies_list.layoutManager = LinearLayoutManager(MainApplication.applicationContext(), LinearLayoutManager.HORIZONTAL, true)


        viewModel.getAllActiveContactsForCurrentTrip().observe(this,
                Observer<List<Contact>> { contactsList ->
                    Log.d("Contact list is initialized, size = ${contactsList.size}, value = $contactsList")
                    displayContactsList(contactsList)
                })

        viewModel.getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip().observe(this,
                Observer<List<Currency>> { currenciesList: List<Currency> ->

                    if (currenciesList.isEmpty()) {
                        Log.d("Currencies list is empty, show alert")
                        showAlertAndGoToCurrencies("Please specify at least one currency for the trip first")
                        return@Observer
                    }

                    val debt = viewModel.getCurrentDebt().value
                    if (debt != null && debt.currencyId != -1) {
                        val currenciesListWithSavedForDebtMarker: List<Currency> = currenciesList.map {originalCurrency ->
                            specifyIsActiveForCurrentDebtForCurrency(originalCurrency, debt)
                        }
                        currenciesAdapter.setCurrencies(currenciesListWithSavedForDebtMarker)
                    } else {
                        currenciesAdapter.setCurrencies(currenciesList)

                        if (currenciesList.first().lastUsedCurrencyId < 1) {
                            viewModel.onCurrencyClick(currenciesList.first().uid)
                        } else {
                            viewModel.onCurrencyClick(currenciesList.first().lastUsedCurrencyId)
                        }
                    }
                })

        viewModel.getCurrentTrip().observe(this,
                Observer<Trip> { trip ->
                    add_debt_fragment_trip_name_tv.text = "Trip: ${trip?.title}"
                })


        viewModel.getSenderForCurrentDebt().observe(this,
                Observer { contact ->
                    add_debt_fragment_sender_autocompletetv.setText(contact.alias)
                })
    }

    private fun setOnClickListeners() {
        debt_fragment_delete_fab.setOnClickListener { view ->
            Log.d("Delete button is pressed")
            showAlertBeforeDelete("Delete debt?")
        }

        debt_fragment_clear_fab.setOnClickListener { view ->
            showAlertBeforeClearDraft("Clear debt draft?")
        }

        add_debt_fragment_date_et.setOnClickListener { view ->
            displayDatePickerDialog()
        }

        add_debt_fragment_time_et.setOnClickListener { view ->
            displayTimePickerDialog()
        }

        add_debt_fragment_amount_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        val newValue = s.toString().replace(",", ".").toDoubleOrNull()
                        viewModel.debtSpentAmountIsChanged(newValue ?: 0.0)
                    } else {
                        viewModel.debtSpentAmountIsChanged(0.0)
                    }
                }
            }
        })


        add_debt_fragment_sender_autocompletetv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        add_debt_fragment_sender_lil.error = null

                        val selectedContact: Contact? = viewModel.getAllActiveContactsForCurrentTrip().value?.find {contact ->
                            contact.alias == s.toString()
                        }

                        if (selectedContact != null) {
                            viewModel.senderIdIsChanged(selectedContact.uid)
                        }
                    }
                }
            }
        })

//        add_debt_fragment_sender_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
//                if (positionToContactIdMap[position] != null && positionToContactIdMap[position]?.uid != null) {
//                    viewModel.senderIdIsChanged(positionToContactIdMap[position]!!.uid)
//                }
//            }
//
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//            }
//
//        }

        add_debt_fragment_date_et.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                viewModel.dateIsChanged(s.toString())
                //add_debt_fragment_time_et.setText("00:00")
            }
        })

        add_debt_fragment_comment_et.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                viewModel.commentIsChanged(s.toString())
            }
        })

        add_debt_fragment_expertmode_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.expertModeSwitchStatusIsChanged(isChecked)

            if (isChecked) {
                Log.d("expert mode ON, display ExpensesForExpertMode")
                displayExpensesForExpertMode()
            } else {
                Log.d("expert mode OFF, display ExpensesForSimpleMode")
                displayExpensesForSimpleMode(false)
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    private fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun finishSelf() {
        finish()
    }

    private fun getTime(): String {
        return add_debt_fragment_time_et.text.toString()
    }

    private fun getDate(): String {
        return add_debt_fragment_date_et.text.toString()
    }

    private fun showProgress() {
        add_debt_fragment_progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        add_debt_fragment_progress_bar.visibility = View.INVISIBLE
    }

    private fun displayDebtData(debt: Debt) {
        this.debtId = debt.uid
        Log.d("Displaying debt = $debt")

        setExpertMode(debt.expertModeIsEnabled)

        if (debt.expertModeIsEnabled) {
            //Do nothing because switch triggers onChange after init
        } else {
            displayExpensesForSimpleMode(true)
        }

        setAmount(debt.spentAmount)

        if (debt.datetime != 0L) {
            setTime(DateFormatter().formatTimeFromTimestamp(debt.datetime))
            setDate(DateFormatter().formatDateFromTimestamp(debt.datetime))
        }

        if (debt.comment != null) {
            setComment(debt.comment!!)
        }

        if (debt.status == STATUS_DRAFT) {
            hideDeleteButton()
            showClearButton()
        } else {
            hideClearButton()
            showDeleteButton()
        }
    }

    private fun setAmount(amount: Double) {
        add_debt_fragment_amount_et.setText(MoneyFormatter.formatAmountForEditableText(amount))
    }

    private fun setDate(date: String) {
        add_debt_fragment_date_et.setText(date)
    }

    private fun setTime(time: String) {
        Log.d("Setting time = $time")
        add_debt_fragment_time_et.setText(time)
    }

    private fun setComment(comment: String) {
        add_debt_fragment_comment_et.setText(comment)
    }

    private fun setExpertMode(isEnabled: Boolean) {
        add_debt_fragment_expertmode_switch.isChecked = isEnabled
    }

    private fun hideDeleteButton() {
        debt_fragment_delete_fab.visibility = View.INVISIBLE
    }

    private fun showDeleteButton() {
        debt_fragment_delete_fab.visibility = View.VISIBLE
    }

    private fun hideClearButton() {
        debt_fragment_clear_fab.visibility = View.INVISIBLE
    }

    private fun showClearButton() {
        debt_fragment_clear_fab.visibility = View.VISIBLE
    }

    private fun displayTimePickerDialog() {
        if (timePickerDialog != null) return

        val mcurrentTime = Calendar.getInstance()

        if (getTime() != null && getTime().isNotEmpty()) {
            mcurrentTime.timeInMillis = DateFormatter().getTimestampFromFormattedDateTime("${getDate()} ${getTime()}")!!
        }

        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)

        timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
            if (selectedMinute.toString().length == 1) {
                add_debt_fragment_time_et.setText("$selectedHour:0$selectedMinute")
                viewModel.timeIsChanged("$selectedHour:0$selectedMinute")
            } else {
                add_debt_fragment_time_et.setText("$selectedHour:${selectedMinute}")
                viewModel.timeIsChanged("$selectedHour:$selectedMinute")
            }
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

            datePickerDialog?.setOnCancelListener {
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
            //ToDO("VERSION.SDK_INT < N")
        }
    }

    private fun showAlertAndGoToContacts(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
            alertDialogBuilder.setPositiveButton("Go to contacts") { _, _ ->
                Log.d("Setting result code to $GO_TO_CONTACTS_RESULT_CODE and finishing self")
                setResult(GO_TO_CONTACTS_RESULT_CODE)
                finish()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun showAlertAndGoToTrip(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
            alertDialogBuilder.setPositiveButton("Go to trip") { _, _ ->
                setResult(GO_TO_CURRENT_TRIP_RESULT_CODE)
                finish()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun showAlertAndGoToCurrencies(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
            alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
                //ToDo NEW открывать вкладку currencies после этого
                finishSelf()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun showAlertBeforeDelete(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
            alertDialogBuilder.setPositiveButton("Delete") { _, _ ->
                viewModel.deleteDebt()
                finishSelf()
            }
            alertDialogBuilder.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                alertDialog?.dismiss()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun showAlertBeforeClearDraft(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
            alertDialogBuilder.setPositiveButton("Clear") { _, _ ->
                viewModel.clearDebtDraft()
            }
            alertDialogBuilder.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                alertDialog?.dismiss()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun doneButtonIsPressed() {
        Log.d("Done button is pressed")
        viewModel.timeIsChanged(add_debt_fragment_time_et.text.toString())
        hideKeyboard()
        viewModel.doneButton()
        finishSelf()
    }

    private fun displayContactsList(contactsList: List<Contact>) {

        if (contactsList.isEmpty() && viewModel.getCurrentDebt().value?.status == STATUS_DRAFT) {
            if (allContactsForAllTripsCount != 0) {
                showAlertAndGoToTrip("Please choose contacts for the trip first")
            } else {
                showAlertAndGoToContacts("Please add at least one contact first")
            }
        }

        val friendsList: List<String> = contactsList.map { contact -> contact.alias }
        setupSenderSpinner(friendsList.toTypedArray())
    }

        private fun setupSenderSpinner(contactsList: Array<String?>) {


            add_debt_fragment_sender_autocompletetv.setOnClickListener {
                add_debt_fragment_sender_autocompletetv.forceFiltering()
            }


            val adapter = ArrayAdapter<String>(
                    MainApplication.applicationContext(),
                    R.layout.dropdown_menu_popup_item,
                    contactsList
            )

            add_debt_fragment_sender_autocompletetv.setAdapter(adapter)

//            if (viewModel.getCurrentDebt().value != null && viewModel.getCurrentDebt().value!!.senderId != -1) {
//                if (contactIdToPositionMap[viewModel.getCurrentDebt().value!!.senderId] == null) return
//                setSender(contactIdToPositionMap[viewModel.getCurrentDebt().value!!.senderId]!!)
//            }
        }

        private fun displayExpensesForExpertMode() {

            if (!add_debt_fragment_expertmode_switch.isChecked) return
            if (viewModel.getCurrentDebt().value?.expertModeIsEnabled == false) return

            if (simpleExpensesFragment!= null && simpleExpensesFragment!!.isAdded) {
                supportFragmentManager.beginTransaction()
                        .remove(simpleExpensesFragment!!)
                        .commit()
            }

            if (extendedExpensesFragment != null && extendedExpensesFragment!!.isAdded) return

            if (extendedExpensesFragment == null || (extendedExpensesFragment != null && !extendedExpensesFragment!!.isAdded)) {

                extendedExpensesFragment = ExtendedExpensesFragment()
                val supportFragmentManager = supportFragmentManager
                supportFragmentManager.beginTransaction()
                        .add(R.id.debt_fragment_container_for_expenses, extendedExpensesFragment!!)
                        //.addToBackStack("DebtsFragment")
                        .commit()

            }
        }

        private fun displayExpensesForSimpleMode(isInitial: Boolean) {

            if (add_debt_fragment_expertmode_switch.isChecked) return
            if (viewModel.getCurrentDebt().value?.expertModeIsEnabled == true) return

            if (expensesListSize == -1) {
                viewModel.getExpensesListForSimpleMode()?.observe(this,
                        Observer { expensesList ->
                            Log.d("Expenses list is received from DB, size = ${expensesList.size}")
                            expensesListSize = expensesList.size
                            this.expensesList.clear()
                            this.expensesList.addAll(expensesList)
                            checkNumberOfExpenseAndDisplaySimpleExpensesFragment(isInitial)
                        })
            } else {
                checkNumberOfExpenseAndDisplaySimpleExpensesFragment(isInitial)
            }
        }

    private fun checkNumberOfExpenseAndDisplaySimpleExpensesFragment(isInitial: Boolean) {

        if (add_debt_fragment_expertmode_switch.isChecked) return
        if (viewModel.getCurrentDebt().value?.expertModeIsEnabled == true) return

        if (extendedExpensesFragment != null && extendedExpensesFragment!!.isAdded) {
            supportFragmentManager.beginTransaction()
                    .remove(extendedExpensesFragment!!)
                    .commit()
        }


        if (simpleExpensesFragment != null && simpleExpensesFragment!!.isAdded) return

        if (simpleExpensesFragment == null || (simpleExpensesFragment != null && !simpleExpensesFragment!!.isAdded)) {

            simpleExpensesFragment = SimpleExpensesFragment()
            if (expensesListSize == 1 && isInitial) {
                val bundle = Bundle()
                expenseIdForSimpleMode = expensesList[0].uid
                bundle.putInt(EXPENSE_ID_KEY, expenseIdForSimpleMode)
                simpleExpensesFragment!!.arguments = bundle
            }

            val supportFragmentManager = supportFragmentManager
            supportFragmentManager.beginTransaction()
                    .add(R.id.debt_fragment_container_for_expenses, simpleExpensesFragment!!)
                    //.addToBackStack("DebtsFragment")
                    .commit()
        }
    }

    private fun clearContainerForExpensesList() {
        if (supportFragmentManager.findFragmentById(R.id.debt_fragment_container_for_expenses) != null) {
            supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.debt_fragment_container_for_expenses)!!).commit()
        }
    }

    private fun specifyIsActiveForCurrentDebtForCurrency(currency: Currency, debt: Debt): Currency {
        currency.isActiveForCurrentTrip = (currency.uid == debt.currencyId)
        return currency
    }

}