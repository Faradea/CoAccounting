package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.presenters.ExpensePresenter
import com.macgavrina.co_accounting.room.Contact
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.*

import kotlinx.android.synthetic.main.expense_activity.*

class ExpenseActivity : AppCompatActivity(), AddReceiverInAddDebtContract.View, TextWatcher {

    lateinit var presenter: ExpensePresenter
    private lateinit var viewManagerForNotSelected: RecyclerView.LayoutManager
    private lateinit var viewManagerForSelected: RecyclerView.LayoutManager
    var debtId: Int? = null
    var expenseId: Int? = null

    companion object {
        const val DEBT_ID_KEY = "debtid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expense_activity)
        setSupportActionBar(toolbar)

        presenter = ExpensePresenter()
        presenter.attachView(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extras = intent.extras
        val debtId = extras?.getInt("debtId")
        val expenseId = extras?.getInt("expenseId")

        if (debtId != null) {
            presenter.debtIdIsReceiverFromMainActivity(debtId)
        }

        if (expenseId != null && expenseId != -1) {
            presenter.expenseIdIsReceivedFromMainActivity(expenseId)
        }

        add_receiver_dialog_fragment_amount_et.addTextChangedListener(this)
    }

    override fun onResume() {
        super.onResume()

        viewManagerForNotSelected = LinearLayoutManager(MainApplication.applicationContext())
        add_receiver_dialog_fragment_receiverlist_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(null)
        add_receiver_dialog_fragment_receiverlist_lv.layoutManager = viewManagerForNotSelected

        viewManagerForSelected = LinearLayoutManager(MainApplication.applicationContext())
        add_receiver_dialog_fragment_selected_members_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(null)
        add_receiver_dialog_fragment_selected_members_lv.layoutManager = viewManagerForSelected

        add_receiver_dialog_fragment_delete_fab.setOnClickListener { _ ->
            Log.d("delete button is pressed")
            presenter.deleteButtonIsPressed()
        }

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
                Log.d("done button is pressed")
                presenter.saveButtonIsPressed()
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

    //EditText listeners
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s != null) {
            if (s.isNotEmpty()) {
                presenter.amountIsEdited(s.toString().replace(",", ".").toFloat())
            } else {
                presenter.amountIsEdited(0.0F)
            }
        }
    }

    override fun initializeNotSelectedReceiversList(contactsList: List<Contact>?) {
        add_receiver_dialog_fragment_receiverlist_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(contactsList)
    }

    override fun initializeSelectedReceiversList(contactsList: List<Contact>?, amountPerPerson: String) {

        if (contactsList == null || contactsList.isEmpty()) {
            add_expense_emplty_selected_list_layout.visibility = View.VISIBLE
        } else {
            add_expense_emplty_selected_list_layout.visibility = View.INVISIBLE
            add_receiver_dialog_fragment_selected_members_lv.adapter = SelectedReceiversRecyclerViewAdapter(contactsList, amountPerPerson)
        }
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun getAmount(): Float {
        val etText = add_receiver_dialog_fragment_amount_et.text
        return if (etText != null && etText.isNotEmpty()) {
            etText.toString().replace(",", ".").toFloat()
        } else {
            0F
        }
    }

    override fun hideDeleteButton() {
        add_receiver_dialog_fragment_delete_fab.hide()
    }

    override fun showDeleteButton() {
        add_receiver_dialog_fragment_delete_fab.show()
    }

    override fun setAmount(totalAmount: String?) {
        add_receiver_dialog_fragment_amount_et.setText(totalAmount)
    }

    override fun finishSelf() {
        onBackPressed()
    }
}
