package com.macgavrina.co_accounting.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.presenters.ExpensePresenter
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.support.MoneyFormatter
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.*

import kotlinx.android.synthetic.main.expense_activity.*

class ExpenseActivity : AppCompatActivity(), AddReceiverInAddDebtContract.View, TextWatcher {

    lateinit var presenter: ExpensePresenter
    private lateinit var viewManagerForNotSelected: RecyclerView.LayoutManager
    private lateinit var viewManagerForSelected: RecyclerView.LayoutManager
    private var selectedReceiversList = mutableListOf<Contact>()
    private var notSelectedReceiversList = mutableListOf<Contact>()
    var debtId: Int? = null
    var expenseId: Int? = null
    private var alertDialog: AlertDialog? = null

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

        if (expenseId == null) {
            presenter.expenseIdIsReceivedFromMainActivity(-1)
        } else {
            presenter.expenseIdIsReceivedFromMainActivity(expenseId)
        }

        add_receiver_dialog_fragment_amount_et.addTextChangedListener(this)

        add_receiver_dialog_fragment_delete_fab.setOnClickListener { _ ->
            showAlertBeforeDelete("Delete expense?")
        }

        viewManagerForNotSelected = LinearLayoutManager(MainApplication.applicationContext())
        simple_expenses_list_notselected_members_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(notSelectedReceiversList)
        simple_expenses_list_notselected_members_lv.layoutManager = viewManagerForNotSelected

        viewManagerForSelected = LinearLayoutManager(MainApplication.applicationContext())
        simple_expenses_list_selected_members_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(selectedReceiversList)
        simple_expenses_list_selected_members_lv.layoutManager = viewManagerForSelected
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
                presenter.amountIsEdited(s.toString().replace(",", ".").toDouble())
            } else {
                presenter.amountIsEdited(0.0)
            }
        }
    }

    override fun initializeNotSelectedReceiversList(contactsList: List<Contact>?) {

        notSelectedReceiversList.clear()

        if (contactsList != null) {
            notSelectedReceiversList.addAll(contactsList)
        }

        simple_expenses_list_notselected_members_lv.adapter?.notifyDataSetChanged()
    }

    override fun initializeSelectedReceiversList(contactsList: List<Contact>?, amountPerPerson: Double) {

        //ToDo REFACT Add amountPerPerson inside selectedReceiversList and update recycler view with notifyDataSetChanged

        simple_expenses_list_selected_members_lv.adapter = SelectedReceiversRecyclerViewAdapter(contactsList, amountPerPerson)
        
        if (contactsList == null || contactsList.isEmpty()) {
            add_expense_emplty_selected_list_layout.visibility = View.VISIBLE
        } else {
            add_expense_emplty_selected_list_layout.visibility = View.INVISIBLE
        }
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun getAmount(): Double {
        val etText = add_receiver_dialog_fragment_amount_et.text
        return if (etText != null && etText.isNotEmpty()) {
            etText.toString().replace(",", ".").toDouble()
        } else {
            0.0
        }
    }

    override fun getComment(): String {
        return add_receiver_dialog_fragment_comment_tv.text.toString()
    }

    override fun setComment(comment: String) {
        add_receiver_dialog_fragment_comment_tv.setText(comment)
    }

    override fun hideDeleteButton() {
        add_receiver_dialog_fragment_delete_fab.visibility = View.INVISIBLE
    }

    override fun showDeleteButton() {
        add_receiver_dialog_fragment_delete_fab.visibility = View.VISIBLE
    }

    override fun setAmount(totalAmount: Double) {
        add_receiver_dialog_fragment_amount_et.setText(MoneyFormatter.formatAmountForEditableText(totalAmount))
    }

    override fun finishSelf() {
        onBackPressed()
    }

    override fun showAlertAndFinishSelf(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
                    .setTitle("Alert")
            alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
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
                presenter.deleteButtonIsPressed()
                finishSelf()
            }
            alertDialogBuilder.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                alertDialog?.dismiss()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }

    override fun showAlertAndFinishSelfWithCallback(alertText: String) {
        if (alertDialog == null || alertDialog?.isShowing == false) {
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(alertText)
                    .setTitle("Alert")
            alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
                presenter.userHasReadAlertAboutDeletingExpense()
            }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        }
    }
}
