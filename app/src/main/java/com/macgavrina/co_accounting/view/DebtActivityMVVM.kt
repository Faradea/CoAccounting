package com.macgavrina.co_accounting.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
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
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.support.DateFormatter
import com.macgavrina.co_accounting.support.MoneyFormatter
import com.macgavrina.co_accounting.viewmodel.DebtViewModel
import com.macgavrina.co_accounting.viewmodel.DebtsViewModel
import com.macgavrina.co_accounting.viewmodel.EXPENSE_ID_KEY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.add_debt_fragment.*
import kotlinx.android.synthetic.main.debt_activity.*
import java.util.*

class DebtActivityMVVM : AppCompatActivity(), DebtCurrenciesRecyclerViewAdapter.OnCurrencyClickListener {

    private lateinit var viewModel: DebtViewModel
    private var debtId: Int = -1

    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null
    private var alertDialog: AlertDialog? = null

    lateinit var contactsIdToNameMap: MutableMap<String, Contact>
    lateinit var positionToContactIdMap: MutableMap<Int, Contact>
    lateinit var contactIdToPositionMap: MutableMap<Int, Int>
    lateinit var friendsList: Array<String?>
    private var senderId: Int? = null

    private var expenseIdForSimpleMode: Int = -1
    private var extendedExpensesFragment: ExtendedExpensesFragment? = null
    private var simpleExpensesFragment: SimpleExpensesFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debt_activity)
        setSupportActionBar(toolbar)

        hideProgress()
        hideClearButton()
        hideDeleteButton()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(DebtViewModel::class.java)

        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                displayToast(res)
            }
        })

        val extras = intent.extras
        if (extras?.getInt("debtId") != null && extras.getInt("debtId") != -1) {
            debtId = extras.getInt("debtId")
            Log.d("debtId is received from intent, debtId = $debtId")
            viewModel.debtIdIsReceivedFromIntent(debtId)
        } else {
            Log.d("debtId isn't received from intent")
            viewModel.debtIdIsNotReceivedFromIntent()
        }


        viewModel.getCurrentDebt().observe(this,
                Observer<Debt> { debt ->
                    if (debt != null) {
                        displayDebtData(debt)
                    }
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
                Observer<List<Currency>> { currenciesList ->

                    if (currenciesList.isEmpty()) {
                        Log.d("Currencies list is empty, show alert")
                        showAlertAndGoToCurrencies("Please specify at least one currency for the trip first")
                    }

                    val debt = viewModel.getCurrentDebt().value
                    if (debt != null && debt.currencyId != -1) {
                        val currenciesListWithSavedForDebtMarker = mutableListOf<Currency>()
                        currenciesList.forEach { currency ->
                            currency.isActiveForCurrentTrip = currency.uid == debt.currencyId
                            currenciesListWithSavedForDebtMarker.add(currency)
                        }
                        currenciesAdapter.setCurrencies(currenciesListWithSavedForDebtMarker)
                    } else {
                        currenciesAdapter.setCurrencies(currenciesList)

                        if (currenciesList[0].lastUsedCurrencyId < 1) {
                            viewModel.onCurrencyClick(currenciesList[0].uid)
                        } else {
                            viewModel.onCurrencyClick(currenciesList[0].lastUsedCurrencyId)
                        }
                    }
                })





        debt_fragment_delete_fab.setOnClickListener { view ->
            Log.d("Delete button is pressed")
            viewModel.deleteDebt()
            finishSelf()
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

        add_debt_fragment_amount_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("Debt spent amount is changed, newValue = $s")
                if (s != null) {
                    if (s.isNotEmpty()) {
                        Log.d("Change ViewModel notSavedDebtSpentAmount value to ${s.toString().replace(",", ".").toDouble()}")
                        val newValue = s.toString().replace(",", ".").toDouble()
                        viewModel.debtSpentAmountIsChanged(newValue)
                    } else {
                        Log.d("Change ViewModel notSavedDebtSpentAmount value to 0.0")
                        viewModel.debtSpentAmountIsChanged(0.0)
                    }
                }
            }
        })

        add_debt_fragment_sender_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("new senderId is selected, position = $position")
                if (positionToContactIdMap[position] != null && positionToContactIdMap[position]?.uid != null) {
                    viewModel.senderIdIsChanged(positionToContactIdMap[position]!!.uid)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        add_debt_fragment_date_et.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                viewModel.dateIsChanged(s.toString())
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
                displayExpensesForSimpleMode()
            }
        }

    }


    override fun onResume() {
        super.onResume()
//
//        if (::friendsList.isInitialized && senderId != null) {
//
//            if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[senderId!!] != null) {
//                setSender(contactIdToPositionMap[senderId!!]!!)
//            }
//        }
    }

    override fun onPause() {
        super.onPause()

//        if (extendedExpensesFragment!= null && extendedExpensesFragment!!.isAdded) {
//            supportFragmentManager.beginTransaction()
//                    .remove(extendedExpensesFragment!!)
//                    .commit()
//        }
//
//        if (simpleExpensesFragment!= null && simpleExpensesFragment!!.isAdded) {
//            supportFragmentManager.beginTransaction()
//                    .remove(simpleExpensesFragment!!)
//                    .commit()
//        }
    }


    //For action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return true
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
        viewModel.onBackPressed()
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
        finish()
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

    private fun getExpertModeFlag(): Boolean {
        return add_debt_fragment_expertmode_switch.isChecked
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
        if (senderId == null) {
            Log.d("Setting sender, debt.senderId = ${debt.senderId}, ::friendsList.isInitialized = ${::friendsList.isInitialized}")
            if (debt.senderId != -1 && ::friendsList.isInitialized) {

                if (::contactIdToPositionMap.isInitialized && debt != null && contactIdToPositionMap?.isNotEmpty() && contactIdToPositionMap[debt.senderId?.toInt()] != null) {
                    setSender(contactIdToPositionMap[debt.senderId?.toInt()]!!)
                }
            }

            if (debt.senderId == -1 && ::friendsList.isInitialized) {
                setSender(0)
            }
        }

        setExpertMode(debt.expertModeIsEnabled)

        if (debt.expertModeIsEnabled) {
            //Do nothing because switch triggers onChange after init
        } else {
            displayExpensesForSimpleMode()
        }

        if (debt.spentAmount != null) {
            setAmount(debt.spentAmount!!)
        }

        if (debt.datetime != null) {
            Log.d("Debt datetime = ${debt.datetime!!}")
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

    private fun setAmount(amount: Double) {
        add_debt_fragment_amount_et.setText(MoneyFormatter.formatAmount(amount))
    }

    private fun setDate(date: String) {
        Log.d("setDate = $date")
        add_debt_fragment_date_et.setText(date)
    }

    private fun setTime(time: String) {
        Log.d("setTime = $time")
        add_debt_fragment_time_et.setText(time)
    }

    private fun setComment(comment: String) {

        add_debt_fragment_comment_et.setText(comment)
    }


    private fun setSender(position: Int) {
        Log.d("Set sender, position = $position")
        //senderId = position
        add_debt_fragment_sender_spinner.setSelection(position)
    }

    private fun setExpertMode(isEnabled: Boolean) {
        add_debt_fragment_expertmode_switch.isChecked = isEnabled
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
            mcurrentTime.timeInMillis = DateFormatter().getTimestampFromFormattedDateTime("${getDate()} ${getTime()}")!!
        }

        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)

        timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
            Log.d("Time picker result: hour = $selectedHour, minute = $selectedMinute")
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
            TODO("VERSION.SDK_INT < N")
        }
    }

    private fun showAlertAndGoToContacts(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
                    .setTitle("Alert")
            alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
                //ToDo NEW открывать вкладку contacts после этого
                finishSelf()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun showAlertAndGoToCurrencies(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
                    .setTitle("Alert")
            alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
                //ToDo NEW открывать вкладку currencies после этого
                finishSelf()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    private fun doneButtonIsPressed() {
        Log.d("Done button is pressed")
        hideKeyboard()
        viewModel.doneButton()
        finishSelf()
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

//            val debt = viewModel.getCurrentDebt()
//            if (debt?.senderId != null && debt.senderId!!.isNotEmpty() && debt.senderId != "null") {
//
//                if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[debt.senderId?.toInt()] != null) {
//                    setSender(contactIdToPositionMap[debt.senderId?.toInt()]!!)
//                }
//            }
//        }
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

            if (viewModel.getCurrentDebt().value != null && viewModel.getCurrentDebt().value!!.senderId != -1) {
                if (contactIdToPositionMap[viewModel.getCurrentDebt().value!!.senderId] == null) return
                setSender(contactIdToPositionMap[viewModel.getCurrentDebt().value!!.senderId]!!)
            }
//            if (senderId != null) {
//                setSender(senderId!!)
//            }
        }

        fun displayExpensesForExpertMode() {

            Log.d("displayExpensesForExpertMode fragment")

            //clearContainerForExpensesList()
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

        fun displayExpensesForSimpleMode() {

            Log.d("displayExpensesForSimpleMode fragment, viewModel.getExpensesList() = ${viewModel.getExpensesList()}")

            //clearContainerForExpensesList()
            viewModel.getExpensesList()?.observe(this,
                    Observer { expensesList ->
                        Log.d("Expenses list for debt is loaded, size = ${expensesList.size}, value = $expensesList")
                        if (expensesList.size > 1) {
                            Log.d("somehow there is simple mode but more than 1 expense, force switch to expert mode")
                            add_debt_fragment_expertmode_switch.isChecked = true
                        }
                        else {

                            if (extendedExpensesFragment != null && extendedExpensesFragment!!.isAdded) {
                                supportFragmentManager.beginTransaction()
                                        .remove(extendedExpensesFragment!!)
                                        .commit()
                            }

                            if (simpleExpensesFragment == null || (simpleExpensesFragment != null && !simpleExpensesFragment!!.isAdded)) {

                                simpleExpensesFragment = SimpleExpensesFragment()
                                if (expensesList.isNotEmpty()) {
                                    val bundle: Bundle = Bundle()
                                    Log.d("expenseIdForSimpleMode = $expenseIdForSimpleMode")
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
                    })

        }

    private fun clearContainerForExpensesList() {
        if (supportFragmentManager.findFragmentById(R.id.debt_fragment_container_for_expenses) != null) {
            supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.debt_fragment_container_for_expenses)!!).commit()
        }
    }

    }