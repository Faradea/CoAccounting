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
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversWithOnClickRecyclerViewAdapter
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

class SimpleExpensesFragment: Fragment(), SelectedReceiversWithOnClickRecyclerViewAdapter.OnSelectedContactClickListener, NotSelectedReceiversWithOnClickRecyclerViewAdapter.OnNotSelectedContactClickListener {

    private var debtId: Int = -1
    private var expenseId: Int = -1
    private var debt: Debt? = null
    private var debtTotalAmount: Double = 0.0
    private val selectedContactsList = mutableListOf<Contact>()
    private val notSelectedContactsList = mutableListOf<Contact>()

    private lateinit var viewModel: DebtsViewModel
    //private var tripsList: MutableList<Trip> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        Log.d("onCreateView")
        return inflater.inflate(R.layout.simple_expenses_list, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("Simple expenses fragment: onActivityCreated")

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(DebtsViewModel::class.java)
        }

        if (arguments?.getInt(DEBT_ID_KEY) != null) {
            debtId = arguments?.getInt(DEBT_ID_KEY)!!
            viewModel.getDebtById(debtId)?.observe(viewLifecycleOwner,
                    Observer {
                        this.debt = it
                        if (debt?.spentAmount != null) {
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
            viewModel.getSelectedContactsForExpense(expenseId).observe(viewLifecycleOwner,
                    Observer { selectedContactsList ->
                        Log.d("getSelectedContactsForExpense result = $selectedContactsList")
                        if (viewModel.notSavedSelectedContactList.value != null && viewModel.notSavedSelectedContactList.value!!.isNotEmpty()) return@Observer
                        this.selectedContactsList.clear()
                            this.selectedContactsList.addAll(selectedContactsList)
                            var amountPerPerson = "0"
                            if (debt?.spentAmount != null) {
                                amountPerPerson = DecimalFormat("##.##").format(debt!!.spentAmount!!.toDouble() / selectedContactsList.size)
                            }
                            initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
                    })
        }

        viewModel.getNotSelectedContactsForExpense(expenseId).observe(viewLifecycleOwner,
                Observer { notSelectedContactsList ->
                    Log.d("getNotSelectedContactsForExpense, result = $notSelectedContactsList")
                    if (viewModel.notSavedNotSelectedContactList.value != null && viewModel.notSavedNotSelectedContactList.value!!.isNotEmpty()) return@Observer
                    this.notSelectedContactsList.clear()
                        this.notSelectedContactsList.addAll(notSelectedContactsList)
                        initializeNotSelectedReceiversList(notSelectedContactsList)
                })

        viewModel.notSavedDebtSpentAmount.observe(viewLifecycleOwner, Observer {
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

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("Simple expenses fragment: onCreate")

        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.viewIsDestroyed()
    }

    override fun onSelectedContactClick(selectedContact: Contact) {
        Log.d("onSelectedContactClick, contact = $selectedContact")
        selectedContactsList.remove(selectedContact)
        setAmountPerPersonForDebtTotal(debtTotalAmount)

        notSelectedContactsList.add(selectedContact)
        initializeNotSelectedReceiversList(notSelectedContactsList)
    }

    override fun onNotSelectedContactClick(selectedContact: Contact) {
        Log.d("onNotSelectedContactClick, contact = $selectedContact")
        selectedContactsList.add(selectedContact)
        setAmountPerPersonForDebtTotal(debtTotalAmount)

        notSelectedContactsList.remove(selectedContact)
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
        Log.d("Update selected contacts list, size = ${contactsList?.size}")
        viewModel.notSavedSelectedContactList.postValue(contactsList)
        if (contactsList == null || contactsList.isEmpty()) {
            simple_expenses_list_empty_list_layout.visibility = View.VISIBLE
        } else {
            simple_expenses_list_empty_list_layout.visibility = View.INVISIBLE
            simple_expenses_list_selected_members_lv.adapter = SelectedReceiversWithOnClickRecyclerViewAdapter(contactsList, amountPerPerson, this)
        }
    }

    private fun initializeNotSelectedReceiversList(contactsList: List<Contact>?) {
        viewModel.notSavedNotSelectedContactList.postValue(contactsList)
        Log.d("initializeNotSelectedReceiversList and set OnClick listener")
        simple_expenses_list_notselected_members_lv.adapter = NotSelectedReceiversWithOnClickRecyclerViewAdapter(contactsList, this)
    }

}