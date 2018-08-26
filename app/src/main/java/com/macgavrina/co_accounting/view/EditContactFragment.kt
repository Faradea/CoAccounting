package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.EditContactContract
import com.macgavrina.co_accounting.presenters.EditContactPresenter
import com.macgavrina.co_accounting.rxjava.LoginInputObserver.LoginInputObserver.getTextWatcherObservable
import kotlinx.android.synthetic.main.edit_contact_fragment.*
import kotlinx.android.synthetic.main.login_fragment.*
import io.reactivex.Observable
import java.util.*

class EditContactFragment: Fragment(), EditContactContract.View {
    override fun setSaveButtonEnabled(isSaveButtonEnabled: Boolean) {
        edit_contact_fragment_save_button.isEnabled = isSaveButtonEnabled
    }

    override fun displayContactData(alias: String, email: String) {
        edit_contact_fragment_alias_et.setText(alias)
        edit_contact_fragment_email_et.setText(email)
    }

    companion object {
        const val CONTACT_UID_KEY = "uid"
    }

    lateinit var presenter: EditContactPresenter
    lateinit var contactUid:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = EditContactPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.edit_contact_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (this.arguments != null) {
            contactUid = this.arguments!!.getString(CONTACT_UID_KEY)
        } else {
            contactUid = ""
        }

        edit_contact_fragment_save_button.setOnClickListener{ view ->
            presenter.saveButtonIsPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        val aliasObservable: Observable<String> = getTextWatcherObservable(edit_contact_fragment_alias_et)
        aliasObservable.subscribe {it ->
            //ToDo делать кнопку Save активной только после изменения alias
            presenter.aliasIsChanged()

        }

        presenter.viewIsReady(contactUid)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun getAliasFromEditText(): String {
        return edit_contact_fragment_alias_et.text.toString()
    }

    override fun showProgress() {
        edit_contact_fragment_progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        edit_contact_fragment_progressBar.visibility = View.INVISIBLE
    }

    override fun displayToast(text:String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}