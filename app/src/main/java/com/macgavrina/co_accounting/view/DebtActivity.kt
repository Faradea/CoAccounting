//package com.macgavrina.co_accounting.view
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.view.inputmethod.InputMethodManager
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.macgavrina.co_accounting.MainApplication
//import com.macgavrina.co_accounting.R
//import com.macgavrina.co_accounting.adapters.ExpensesRecyclerViewAdapter
//import com.macgavrina.co_accounting.interfaces.DebtActivityContract
//import com.macgavrina.co_accounting.presenters.DebtActivityPresenter
//import com.macgavrina.co_accounting.room.Expense
//import com.macgavrina.co_accounting.support.DateFormatter
//import kotlinx.android.synthetic.main.add_debt_fragment.*
//import kotlinx.android.synthetic.main.debt_activity.*
//import java.util.*
//
//
//class DebtActivity : AppCompatActivity(), DebtActivityContract.View {
//
//
//    //private var senderId: Int? = null
//    lateinit var presenter: DebtActivityPresenter
//    private lateinit var viewManager: RecyclerView.LayoutManager
//    private var expenseList = mutableListOf<Expense>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.debt_activity)
//        setSupportActionBar(toolbar)
//
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        presenter = DebtActivityPresenter()
//        presenter.attachView(this)
//        presenter.viewIsCreated()
//
//
////        val extras = intent.extras
////        val debtId = extras?.getInt("debtId")
////
////        //val debtId = savedInstanceState?.getInt(AddReceiverInAddDebtFragment.DEBT_ID_KEY)
////        if (debtId == -1) {
////            presenter.debtIdIsReceiverFromMainActivity(null)
////        } else {
////            presenter.debtIdIsReceiverFromMainActivity(debtId)
////        }
//
////        add_debt_fragment_add_receiver_tv.setOnClickListener { view ->
////            presenter.addExpenseButtonIsPressed()
////        }
////
////        debt_fragment_delete_fab.setOnClickListener { view ->
////            presenter.deleteButtonIsPressed()
////        }
////
////        debt_fragment_clear_fab.setOnClickListener { view ->
////            presenter.clearButtonIsPressed()
////        }
////
////        add_debt_fragment_date_et.setOnClickListener { view ->
////            displayDatePickerDialog()
////        }
////
////        add_debt_fragment_time_et.setOnClickListener { view ->
////            displayTimePickerDialog()
////        }
//
////        viewManager = LinearLayoutManager(MainApplication.applicationContext())
////        add_debt_fragment_reciever_recyclerview.adapter = ExpensesRecyclerViewAdapter(expenseList)
////        add_debt_fragment_reciever_recyclerview.layoutManager = viewManager
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        presenter.viewIsReady()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        presenter.viewIsPaused()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        presenter.detachView()
//    }
//
//    //For back button in action bar
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        when (item.itemId) {
//            R.id.action_menu_done -> {
//                presenter.doneButtonIsPressed()
//                return true
//            }
//        }
//
//        onBackPressed()
//        return true
//    }
//
//    override fun onBackPressed() {
//        presenter.saveDebtDraft()
//        super.onBackPressed()
//    }
//
//    //For action bar
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_done, menu)
//        return true
//    }
//
//
//    override fun displayToast(text: String) {
//        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
//    }
////
////    override fun setSender(position: Int) {
////
////        senderId = position
////        add_debt_fragment_sender_spinner.setSelection(position)
////    }
//
////    override fun setAmount(amount: String) {
////
////        add_debt_fragment_amount_et.setText(amount)
////    }
////
////    override fun setDate(date: String) {
////
////        add_debt_fragment_date_et.setText(date)
////    }
////
////    override fun setTime(time: String) {
////
////        add_debt_fragment_time_et.setText(time)
////    }
////
////    override fun setComment(comment: String) {
////
////        add_debt_fragment_comment_et.setText(comment)
////    }
//
//    override fun getSender(): Int {
//        return add_debt_fragment_sender_spinner.selectedItemPosition
//    }
//
//    override fun getAmount(): String {
//        return add_debt_fragment_amount_et.text.toString()
//    }
//
//    override fun getTime(): String {
//        return add_debt_fragment_time_et.text.toString()
//    }
//
//    override fun getDate(): String {
//        return add_debt_fragment_date_et.text.toString()
//    }
//
//    override fun getComment(): String {
//        return add_debt_fragment_comment_et.text.toString()
//    }
//
//    override fun showProgress() {
//        add_debt_fragment_progress_bar.visibility = View.VISIBLE
//    }
//
//    override fun hideProgress() {
//        add_debt_fragment_progress_bar.visibility = View.INVISIBLE
//    }
//
//    override fun hideKeyboard() {
//        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
//    }
//
//    override fun setupSenderSpinner(contactsList: Array<String?>) {
//
//        val adapter = ArrayAdapter<String>(
//                MainApplication.applicationContext(),
//                android.R.layout.simple_spinner_item,
//                contactsList
//        )
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//        add_debt_fragment_sender_spinner.adapter = adapter
//
//        if (senderId != null) {
//            setSender(senderId!!)
//        }
//    }
//
////    override fun initializeExpensesList(inputExpenseList: List<Expense>?) {
////
////        expenseList.clear()
////        if (inputExpenseList != null) {
////            expenseList.addAll(inputExpenseList)
////        }
////
////        if (expenseList == null || expenseList.isEmpty()) {
////            add_debt_fragment_empty_list_layout.visibility = View.VISIBLE
////        } else {
////            add_debt_fragment_empty_list_layout.visibility = View.INVISIBLE
////            add_debt_fragment_reciever_recyclerview.adapter?.notifyDataSetChanged()
////        }
////    }
//
////    override fun finishSelf() {
////        onBackPressed()
////    }
//
//
//
//
//
////    override fun hideDeleteButton() {
////        debt_fragment_delete_fab.hide()
////    }
////
////    override fun showDeleteButton() {
////        debt_fragment_delete_fab.show()
////    }
////
////    override fun hideClearButton() {
////        debt_fragment_clear_fab.hide()
////    }
////
////    override fun showClearButton() {
////        debt_fragment_clear_fab.show()
////    }
//}