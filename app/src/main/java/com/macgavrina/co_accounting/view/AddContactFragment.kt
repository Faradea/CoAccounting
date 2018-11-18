package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.AddContactContract
import com.macgavrina.co_accounting.presenters.AddContactPresenter
import com.macgavrina.co_accounting.rxjava.LoginInputObserver
import io.reactivex.Observable
import kotlinx.android.synthetic.main.add_contact_fragment.*

class AddContactFragment: Fragment(), AddContactContract.View {
    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    lateinit var presenter: AddContactPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = AddContactPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.add_contact_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        add_contact_fragment_add_button.setOnClickListener { view ->
            presenter.addButtonIsPressed()
        }

    }

    override fun onResume() {
        super.onResume()

        val emailObservable: Observable<String> = LoginInputObserver.getTextWatcherObservable(add_contact_fragment_email_et)
        emailObservable.subscribe { it ->
            presenter.inputTextFieldsAreEmpty(it.isNotEmpty())
        }

        presenter.viewIsReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun getAlias(): String {
        return add_debt_fragment_comment_et.text.toString()
    }

    override fun getEmail(): String {
        return add_contact_fragment_email_et.text.toString()
    }

    override fun showProgress() {
        add_contact_fragment_progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        add_contact_fragment_progressBar.visibility = View.INVISIBLE
    }

    override fun setAddButtonEnabled(areEnabled: Boolean) {
        add_contact_fragment_add_button.isEnabled = areEnabled
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}