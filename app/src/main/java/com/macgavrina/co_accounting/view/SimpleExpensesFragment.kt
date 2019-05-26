package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversWithOnClickRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.viewmodel.DebtsViewModel
import com.macgavrina.co_accounting.viewmodel.EXPENSE_ID_KEY
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.*
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.simple_expenses_list_notselected_members_lv
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.simple_expenses_list_selected_members_lv
import kotlinx.android.synthetic.main.simple_expenses_list.*
import java.text.DecimalFormat

class SimpleExpensesFragment: Fragment(), SelectedReceiversWithOnClickRecyclerViewAdapter.OnSelectedContactClickListener {

    private var debtId: Int = -1
    private var expenseId: Int = -1
    private var debt: Debt? = null
    private var debtTotalAmount: Double = 0.0
    var notSelectedContactsList = mutableListOf<Contact>()
    var selectedContactsList = mutableListOf<Contact>()

    private lateinit var viewModel: DebtsViewModel
    //private var tripsList: MutableList<Trip> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.simple_expenses_list, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(DebtsViewModel::class.java)
        }

        if (arguments?.getInt(DEBT_ID_KEY) != null) {
            debtId = arguments?.getInt(DEBT_ID_KEY)!!
            viewModel.getDebtById(debtId)?.observe(this,
                    Observer {
                        this.debt = it
                        if (!debt?.spentAmount.isNullOrEmpty()) {
                            setAmountPerPersonForDebtTotal(debt!!.spentAmount!!.toDouble())
                        }
                    })
        } else {
            return
        }

        val viewManagerForSelected = LinearLayoutManager(MainApplication.applicationContext())
        simple_expenses_list_selected_members_lv.layoutManager = viewManagerForSelected

        val viewManagerForNotSelected = LinearLayoutManager(MainApplication.applicationContext())
        simple_expenses_list_notselected_members_lv.layoutManager = viewManagerForNotSelected

        if (arguments?.getInt(EXPENSE_ID_KEY) != null) {
            expenseId = arguments?.getInt(EXPENSE_ID_KEY)!!
            viewModel.getSelectedContactsForExpense(expenseId).observe(this,
                    Observer { selectedContactsList ->
                        Log.d("getSelectedContactsForExpense result = $selectedContactsList")
                        this.selectedContactsList.clear()
                        this.selectedContactsList.addAll(selectedContactsList)
                        var amountPerPerson = "0"
                        if (!debt?.spentAmount.isNullOrEmpty()) {
                            amountPerPerson = DecimalFormat("##.##").format(debt!!.spentAmount!!.toDouble()/selectedContactsList.size)
                        }
                        initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
                    })
        }

        viewModel.getNotSelectedContactsForExpense(expenseId).observe(this,
                Observer { notSelectedContactsList ->
                    Log.d("getNotSelectedContactsForExpense result = $notSelectedContactsList")
                    this.notSelectedContactsList.clear()
                    this.notSelectedContactsList.addAll(notSelectedContactsList)
                    initializeNotSelectedReceiversList(notSelectedContactsList)
                })

        viewModel.notSavedDebtSpentAmount.observe(this, Observer {
            Log.d("Spent amount is changed for debt, new value = $it, handle this update by simple expense fragment")
            setAmountPerPersonForDebtTotal(it)
        })

//        val adapter = ExpensesRecyclerViewAdapter(this)
//        add_debt_fragment_reciever_recyclerview.adapter = adapter
//        add_debt_fragment_reciever_recyclerview.layoutManager = LinearLayoutManager(MainApplication.applicationContext())
//
//        viewModel.getAllExpensesForDebt(debtId).observe(this,
//                Observer<List<Expense>> { expensesList ->
//                    adapter.setExpenses(expensesList)
//
//                    if (expensesList.isEmpty()) {
//                        add_debt_fragment_empty_list_layout.visibility = View.VISIBLE
//                    } else {
//                        add_debt_fragment_empty_list_layout.visibility = View.INVISIBLE
//                    }
//                })
//

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.viewIsDestroyed()
    }

    override fun onSelectedContactClick(selectedContact: Contact) {
        Log.d("onSelectedContactClick, contactId = $selectedContact")
        selectedContactsList.remove(selectedContact)
        setAmountPerPersonForDebtTotal(debtTotalAmount)

        notSelectedContactsList.add(selectedContact)
        initializeNotSelectedReceiversList(notSelectedContactsList)

    }

    private fun setAmountPerPersonForDebtTotal(debtTotal: Double) {
        this.debtTotalAmount = debtTotal
        Log.d("Amount is edited, new value = $debtTotal")
        if (selectedContactsList.isNotEmpty()) {
            val amountPerPerson = DecimalFormat("##.##").format(debtTotal/selectedContactsList.size)
            initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
        }
    }

    private fun initializeSelectedReceiversList(contactsList: List<Contact>?, amountPerPerson: String) {
        if (contactsList == null || contactsList.isEmpty()) {
            simple_expenses_list_empty_list_layout.visibility = View.VISIBLE
        } else {
            simple_expenses_list_empty_list_layout.visibility = View.INVISIBLE
            simple_expenses_list_selected_members_lv.adapter = SelectedReceiversWithOnClickRecyclerViewAdapter(contactsList, amountPerPerson, this)
        }
    }

    private fun initializeNotSelectedReceiversList(contactsList: List<Contact>?) {
        simple_expenses_list_notselected_members_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(contactsList)
    }

}