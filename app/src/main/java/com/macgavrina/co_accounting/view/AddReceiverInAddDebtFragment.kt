package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.AddRecieverRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.presenters.AddReceiverInAddDebtPresenter
import com.macgavrina.co_accounting.room.Contact
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.*
import kotlinx.android.synthetic.main.contacts_fragment.*

class AddReceiverInAddDebtFragment: Fragment(), AddReceiverInAddDebtContract.View {

    lateinit var presenter: AddReceiverInAddDebtPresenter
    private lateinit var viewManagerForNotSelected: RecyclerView.LayoutManager
    private lateinit var viewManagerForSelected: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = AddReceiverInAddDebtPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.add_receiver_dialog_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



//        add_debt_fragment_add_button.setOnClickListener { view ->
//            presenter.addButtonIsPressed()
//        }
//
//        add_debt_fragment_add_receiver_tv.setOnClickListener { view ->
//            presenter.addReceiverButtonIsPressed()
//        }

    }

    override fun initializeNotSelectedReceiversList(contactsList: List<Contact>?) {
        add_receiver_dialog_fragment_receiverlist_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(contactsList)
    }

    override fun initializeSelectedReceiversList(contactsList: List<Contact>?) {
        add_receiver_dialog_fragment_selected_members_lv.adapter = SelectedReceiversRecyclerViewAdapter(contactsList)
    }

    override fun onResume() {
        super.onResume()

        viewManagerForNotSelected = LinearLayoutManager(MainApplication.applicationContext())
        add_receiver_dialog_fragment_receiverlist_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(null)
        add_receiver_dialog_fragment_receiverlist_lv.layoutManager = viewManagerForNotSelected

        viewManagerForSelected = LinearLayoutManager(MainApplication.applicationContext())
        add_receiver_dialog_fragment_selected_members_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(null)
        add_receiver_dialog_fragment_selected_members_lv.layoutManager = viewManagerForSelected

        presenter.viewIsReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

//    override fun showProgress() {
//        add_debt_fragment_progress_bar.visibility = View.VISIBLE
//    }
//
//    override fun hideProgress() {
//        add_debt_fragment_progress_bar.visibility = View.INVISIBLE
//    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}