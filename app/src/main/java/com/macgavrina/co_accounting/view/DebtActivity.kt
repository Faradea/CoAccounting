package com.macgavrina.co_accounting.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.ExpensesRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.DebtActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.presenters.DebtActivityPresenter
import com.macgavrina.co_accounting.room.Expense
import kotlinx.android.synthetic.main.add_debt_fragment.*

import kotlinx.android.synthetic.main.debt_activity.*

class DebtActivity : AppCompatActivity(), DebtActivityContract.View {

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
        Log.d("debtId = $debtId")
        if (debtId == -1) {
            presenter.debtIdIsReceiverFromMainActivity(null)
        } else {
            presenter.debtIdIsReceiverFromMainActivity(debtId)
        }

        add_debt_fragment_add_receiver_tv.setOnClickListener { view ->
            presenter.addReceiverButtonIsPressed()
        }

        debt_fragment_delete_fab.setOnClickListener { view ->
            presenter.deleteButtonIsPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        //ToDo добавить проверку заполнения обязательных полей
//        val emailObservable: Observable<String> = LoginInputObserver.getTextWatcherObservable(add_contact_fragment_email_et)
//        emailObservable.subscribe { it ->
//            presenter.inputTextFieldsAreEmpty(it.isNotEmpty())
//        }

        viewManager = LinearLayoutManager(MainApplication.applicationContext())

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
                presenter.addButtonIsPressed()
                return true
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


    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun setSender(senderName: String) {
        //ToDo setup sender with value saved in DB
        //add_debt_fragment_sender_spinner.selectedItem
    }

    override fun setAmount(amount: String) {
        add_debt_fragment_amount_et.setText(amount)
    }

    override fun setDate(date: String) {
        add_debt_fragment_date_et.setText(date)
    }

    override fun setComment(comment: String) {
        add_debt_fragment_comment_et.setText(comment)
    }

    override fun getSender(): String {
        return add_debt_fragment_sender_spinner.selectedItem.toString()
    }

    override fun getReceiver(): String {
        //return add_debt_fragment_receiver_spinner.selectedItem.toString()
        return ""
    }

    override fun getAmount(): String {
        return add_debt_fragment_amount_et.text.toString()
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

    override fun setAddButtonEnabled(areEnabled: Boolean) {
        //ToDo
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
    }

    override fun initializeExpensesList(expenseList: List<Expense>) {
        add_debt_fragment_reciever_recyclerview.adapter = ExpensesRecyclerViewAdapter(expenseList)
        add_debt_fragment_reciever_recyclerview.layoutManager = viewManager
    }

    override fun finishSelf() {
        finish()
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
}