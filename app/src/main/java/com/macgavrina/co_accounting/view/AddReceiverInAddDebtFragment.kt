package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.NotSelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.adapters.SelectedReceiversRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.presenters.AddReceiverInAddDebtPresenter
import com.macgavrina.co_accounting.room.Contact
import kotlinx.android.synthetic.main.add_receiver_dialog_fragment.*

class AddReceiverInAddDebtFragment: Fragment(), AddReceiverInAddDebtContract.View, TextWatcher {

    lateinit var presenter: AddReceiverInAddDebtPresenter
    private lateinit var viewManagerForNotSelected: RecyclerView.LayoutManager
    private lateinit var viewManagerForSelected: RecyclerView.LayoutManager
    var debtId: Int? = null

    companion object {
        const val DEBT_ID_KEY = "debtid"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = AddReceiverInAddDebtPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.add_receiver_dialog_fragment, container,
                false)
    }


    //EditText listeners
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s != null) {
            presenter.amountIsEdited(s.toString().toFloat())
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        debtId = this.arguments?.getInt(DEBT_ID_KEY)
        if (debtId != null) {
            presenter.debtIdIsReceiverFromMainActivity(debtId!!)
        }

        add_receiver_dialog_fragment_amount_et.addTextChangedListener(this)

        add_receiver_dialog_fragment_toolbar_cancel_image.setOnClickListener {
            presenter.cancelButtonInToolbarIsClicked()
        }

        add_receiver_dialog_fragment_toolbar_save_button.setOnClickListener {
            presenter.saveButtonIsPressed()
        }

    }

    override fun initializeNotSelectedReceiversList(contactsList: List<Contact>?) {
        add_receiver_dialog_fragment_receiverlist_lv.adapter = NotSelectedReceiversRecyclerViewAdapter(contactsList)
    }

    override fun initializeSelectedReceiversList(contactsList: List<Contact>?, amountPerPerson: Float) {
        add_receiver_dialog_fragment_selected_members_lv.adapter = SelectedReceiversRecyclerViewAdapter(contactsList, amountPerPerson)
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

    override fun getAmount(): Float {
        val etText = add_receiver_dialog_fragment_amount_et.text
        return if (etText.isNotEmpty()) {
            etText.toString().toFloat()
        } else {
            0F
        }
    }
}