package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.ExpensesRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.AddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.ReceiverWithAmount
import com.macgavrina.co_accounting.presenters.AddDebtPresenter
import com.macgavrina.co_accounting.room.Expense
import kotlinx.android.synthetic.main.add_debt_fragment.*


class AddDebtFragment: Fragment(), AddDebtContract.View {

    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    lateinit var presenter: AddDebtPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = AddDebtPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.add_debt_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val debtId = this.arguments?.getInt(AddReceiverInAddDebtFragment.DEBT_ID_KEY)
        Log.d("debtId from bundle = $debtId")
        presenter.debtIdIsReceiverFromMainActivity(debtId)

        add_debt_fragment_add_button.setOnClickListener { view ->
            presenter.addButtonIsPressed()
        }

        add_debt_fragment_add_receiver_tv.setOnClickListener { view ->
            presenter.addReceiverButtonIsPressed()
        }

    }

    override fun onResume() {
        super.onResume()

        //ToDo добавить проверку заполнения обязательных полей
//        val emailObservable: Observable<String> = LoginInputObserver.getTextWatcherObservable(add_contact_fragment_email_et)
//        emailObservable.subscribe { it ->
//            presenter.inputTextFieldsAreEmpty(it.isNotEmpty())
//        }

        viewManager = LinearLayoutManager(MainApplication.applicationContext())

        presenter.viewIsReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun setSender(senderName: String) {
            //ToDo setup sender with value saved in DB
            //add_debt_fragment_sender_spinner.selectedItem
    }

    override fun setAmount(amount: String) {
        add_debt_fragment_amount_et.setText(amount)
    }

    override fun setDate(date: String) {
        add_debt_fragment_date_et.setText(date)
    }

    override fun setComment(comment: String) {
        add_debt_fragment_comment_et.setText(comment)
    }

    override fun getSender(): String {
        return add_debt_fragment_sender_spinner.selectedItem.toString()
    }

    override fun getReceiver(): String {
        //return add_debt_fragment_receiver_spinner.selectedItem.toString()
        return ""
    }

    override fun getAmount(): String {
        return add_debt_fragment_amount_et.text.toString()
    }

    override fun getDate(): String {
        return add_debt_fragment_date_et.text.toString()
    }

    override fun getComment(): String {
        return add_debt_fragment_comment_et.text.toString()
    }

    override fun showProgress() {
        add_debt_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        add_debt_fragment_progress_bar.visibility = View.INVISIBLE
    }

    override fun setAddButtonEnabled(areEnabled: Boolean) {
        add_debt_fragment_add_button.isEnabled = areEnabled
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun setupSenderSpinner(contactsList: Array<String?>) {

        val adapter = ArrayAdapter<String>(
                MainApplication.applicationContext(),
                android.R.layout.simple_spinner_item,
                contactsList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        add_debt_fragment_sender_spinner.adapter = adapter
    }

    override fun initializeExpensesList(expenseList: List<Expense>) {
        add_debt_fragment_reciever_recyclerview.adapter = ExpensesRecyclerViewAdapter(expenseList)
        add_debt_fragment_reciever_recyclerview.layoutManager = viewManager
    }
}