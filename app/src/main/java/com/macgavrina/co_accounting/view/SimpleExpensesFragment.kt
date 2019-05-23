package com.macgavrina.co_accounting.view

import android.content.Intent
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
import com.macgavrina.co_accounting.adapters.ExpensesRecyclerViewAdapter
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.viewmodel.DebtsViewModel
import kotlinx.android.synthetic.main.expended_expenses_list.*

class SimpleExpensesFragment: Fragment() {

    private var debtId: Int = -1

    private lateinit var viewModel: DebtsViewModel
    //private var tripsList: MutableList<Trip> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.simple_expenses_list, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (arguments?.getInt(DEBT_ID_KEY) != null) {
            debtId = arguments?.getInt(DEBT_ID_KEY)!!
        } else {
            return
        }

        viewModel = ViewModelProviders.of(this).get(DebtsViewModel::class.java)

        viewModel.getReceiversForOnlyOneExpenseForDebt(debtId).observe(this,
                Observer<List<Contact>> { contactList ->
                    Log.d("getReceiversForOnlyOneExpenseForDebt result: contactList size = ${contactList.size}, contactList = $contactList")
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