package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDB
import com.macgavrina.co_accounting.viewmodel.DebtsViewModel
import com.macgavrina.co_accounting.viewmodel.EXPENSE_ID_KEY
import kotlinx.android.synthetic.main.add_debt_fragment.*
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.*

class SimpleExpensesFragment: Fragment() {

    private var debtId: Int = -1
    private var expenseId: Int = -1
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

        viewModel = ViewModelProviders.of(this).get(DebtsViewModel::class.java)

        if (arguments?.getInt(DEBT_ID_KEY) != null) {
            debtId = arguments?.getInt(DEBT_ID_KEY)!!
        } else {
            return
        }

        if (arguments?.getInt(EXPENSE_ID_KEY) != null) {
            expenseId = arguments?.getInt(EXPENSE_ID_KEY)!!
            viewModel.getSelectedContactsForExpense(expenseId).observe(this,
                    Observer { selectedContactsList ->
                        Log.d("getSelectedContactsForExpense result = $selectedContactsList")
                        this.selectedContactsList.clear()
                        this.selectedContactsList.addAll(selectedContactsList)
                        val viewManagerForSelected = LinearLayoutManager(MainApplication.applicationContext())
                        simple_expenses_list_selected_members_lv.adapter = SelectedReceiversRecyclerViewAdapter(selectedContactsList, "0")
                        simple_expenses_list_selected_members_lv.layoutManager = viewManagerForSelected
                    })
        }

        viewModel.getNotSelectedContactsForExpense(expenseId).observe(this,
                Observer { notSelectedContactsList ->
                    Log.d("getNotSelectedContactsForExpense result = $notSelectedContactsList")
                    this.notSelectedContactsList.clear()
                    this.notSelectedContactsList.addAll(notSelectedContactsList)
                    val viewManagerForNotSelected = LinearLayoutManager(MainApplication.applicationContext())
                    simple_expenses_list_notselected_members_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(notSelectedContactsList)
                    simple_expenses_list_notselected_members_lv.layoutManager = viewManagerForNotSelected
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

}