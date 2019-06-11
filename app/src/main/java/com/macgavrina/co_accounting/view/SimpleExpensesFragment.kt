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
import com.macgavrina.co_accounting.viewmodel.DebtViewModel
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

    private lateinit var viewModel: DebtViewModel
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
            viewModel = ViewModelProviders.of(it).get(DebtViewModel::class.java)
        }

        if (arguments?.getInt(DEBT_ID_KEY) != null) {
            debtId = arguments?.getInt(DEBT_ID_KEY)!!
            viewModel.getCurrentDebt().observe(viewLifecycleOwner,
                    Observer {debt ->
                        if (debt?.spentAmount != null) {
                            setAmountPerPersonForDebtTotal(debt.spentAmount)
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
            viewModel.expenseIdForSimpleModeIsReceivedFromIntent(expenseId)
        } else {
            viewModel.expenseIdForSimpleModeIsNotReceivedFromIntent()
        }


        viewModel.getSelectedContactsForExpense().observe(viewLifecycleOwner,
                Observer { selectedContactsList ->
                    var amountPerPerson = "0"
                    if (viewModel.getCurrentDebt().value?.spentAmount != null) {
                        amountPerPerson = DecimalFormat("##.##").format(viewModel.getCurrentDebt().value!!.spentAmount / selectedContactsList.size)
                    }
                    initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
                })


        viewModel.getNotSelectedContactsForExpense().observe(viewLifecycleOwner,
                Observer { notSelectedContactsList ->
                    initializeNotSelectedReceiversList(notSelectedContactsList)
                })

        viewModel.getDebtSpentAmountForSimpleExpense().observe(viewLifecycleOwner,
                Observer { spentAmount ->
                    setAmountPerPersonForDebtTotal(spentAmount)
                })
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("Simple expenses fragment: onCreate")

        super.onCreate(savedInstanceState)
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        viewModel.viewIsDestroyed()
//    }

    override fun onSelectedContactClick(selectedContact: Contact) {
        Log.d("onSelectedContactClick, contact = $selectedContact")
        viewModel.selectedContactIsClicked(selectedContact)
    }

    override fun onNotSelectedContactClick(selectedContact: Contact) {
        Log.d("onNotSelectedContactClick, contact = $selectedContact")
        viewModel.notSelectedContactIsClicked(selectedContact)
    }

    private fun setAmountPerPersonForDebtTotal(debtTotal: Double) {
        Log.d("Amount is edited, new value = $debtTotal")
        if (viewModel.getSelectedContactsForExpense().value != null && viewModel.getSelectedContactsForExpense().value!!.isNotEmpty()) {
            val amountPerPerson = DecimalFormat("##.##").format(debtTotal/viewModel.getSelectedContactsForExpense().value!!.size)
            initializeSelectedReceiversList(viewModel.getSelectedContactsForExpense().value!!, amountPerPerson)
        }
    }

    private fun initializeSelectedReceiversList(contactsList: List<Contact>?, amountPerPerson: String) {
        Log.d("Update selected contacts list, size = ${contactsList?.size}")
        //viewModel.notSavedSelectedContactList.postValue(contactsList)
        simple_expenses_list_selected_members_lv.adapter = SelectedReceiversWithOnClickRecyclerViewAdapter(contactsList, amountPerPerson, this)
        if (contactsList == null || contactsList.isEmpty()) {
            simple_expenses_list_empty_list_layout.visibility = View.VISIBLE
        } else {
            simple_expenses_list_empty_list_layout.visibility = View.INVISIBLE
        }
    }

    private fun initializeNotSelectedReceiversList(contactsList: List<Contact>?) {
        //viewModel.notSavedNotSelectedContactList.postValue(contactsList)
        Log.d("initializeNotSelectedReceiversList and set OnClick listener")
        simple_expenses_list_notselected_members_lv.adapter = NotSelectedReceiversWithOnClickRecyclerViewAdapter(contactsList, this)
    }

}