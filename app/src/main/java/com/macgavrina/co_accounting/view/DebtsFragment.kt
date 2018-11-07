package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.adapters.DebtsRecyclerViewAdapter
import com.macgavrina.co_accounting.interfaces.DebtsContract
import com.macgavrina.co_accounting.presenters.DebtsPresenter
import com.macgavrina.co_accounting.room.Debt
import kotlinx.android.synthetic.main.debts_fragment.*

class DebtsFragment: Fragment(), DebtsContract.View {

    lateinit var presenter: DebtsPresenter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = DebtsPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.debts_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        debts_fragment_add_fab.setOnClickListener { view ->
            presenter.addDebtButtonIsPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        viewManager = LinearLayoutManager(MainApplication.applicationContext())
        debts_fragment_recyclerview.adapter = DebtsRecyclerViewAdapter(null)
        debts_fragment_recyclerview.layoutManager = viewManager
        presenter.viewIsReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun displayRevertChangesAction() {
    }

    override fun initializeList(debtsList: List<Debt>) {
        debts_fragment_recyclerview.adapter = DebtsRecyclerViewAdapter(debtsList)
    }

    override fun updateList() {
    }

    override fun showProgress() {
        debts_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        debts_fragment_progress_bar.visibility = View.INVISIBLE
    }

}