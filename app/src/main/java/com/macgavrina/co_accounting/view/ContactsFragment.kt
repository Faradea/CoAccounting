package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.ContactsRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.presenters.ContactsPresenter
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.room.Contact
import kotlinx.android.synthetic.main.contacts_fragment.*

class ContactsFragment: Fragment(), ContactsContract.View {

    lateinit var presenter: ContactsPresenter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = ContactsPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.contacts_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contacts_fragment_add_fab.setOnClickListener { view ->
            presenter.addContactButtonIsPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        viewManager = LinearLayoutManager(MainApplication.applicationContext())
        contacts_fragment_recyclerview.adapter = ContactsRecyclerViewAdapter(null)
        contacts_fragment_recyclerview.layoutManager = viewManager

        presenter.viewIsReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun displayRevertChangesAction() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initializeList(contactsList: List<Contact>) {
        contacts_fragment_recyclerview.adapter = ContactsRecyclerViewAdapter(contactsList)
        contacts_fragment_recyclerview.layoutManager = viewManager
    }

    override fun updateList() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showProgress() {
        contacts_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        contacts_fragment_progress_bar.visibility = View.INVISIBLE
    }

}