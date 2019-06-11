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
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.viewmodel.DebtsViewModel
import kotlinx.android.synthetic.main.expended_expenses_list.*

const val DEBT_ID_KEY = "debtId"

class ExtendedExpensesFragment: Fragment(), ExpensesRecyclerViewAdapter.OnExpenseInDebtClickListener {

    private var debtId: Int = -1

    private lateinit var viewModel: DebtsViewModel
    //private var tripsList: MutableList<Trip> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.expended_expenses_list, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("ExtendedExpensesFragment: onActivityCreated")

        if (arguments?.getInt(DEBT_ID_KEY) != null) {
            debtId = arguments?.getInt(DEBT_ID_KEY)!!
        } else {
            return
        }

        viewModel = ViewModelProviders.of(this).get(DebtsViewModel::class.java)

        val adapter = ExpensesRecyclerViewAdapter(this)
        add_debt_fragment_reciever_recyclerview.adapter = adapter
        add_debt_fragment_reciever_recyclerview.layoutManager = LinearLayoutManager(MainApplication.applicationContext())

        viewModel.getAllExpensesForDebt(debtId).observe(this,
                Observer<List<Expense>> { expensesList ->
                    adapter.setExpenses(expensesList)

                    if (expensesList.isEmpty()) {
                        add_debt_fragment_empty_list_layout.visibility = View.VISIBLE
                    } else {
                        add_debt_fragment_empty_list_layout.visibility = View.INVISIBLE
                    }
                })

        add_debt_fragment_add_receiver_tv.setOnClickListener { view ->
            if (debtId != null && debtId != -1) {
                displayExpenseActivity(debtId, null)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.viewIsDestroyed()
    }

    override fun onExpenseClick(expense: Expense) {
        val intent = Intent()
        intent.action = "com.macgavrina.indebt.EXPENSE"
        intent.putExtra("debtId", debtId)
        intent.putExtra("expenseId", expense.uid)

        startActivity(intent)
    }

    private fun displayExpenseActivity(debtId: Int, expenseId: Int?) {

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