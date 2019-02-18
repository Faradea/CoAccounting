//package com.macgavrina.co_accounting.view
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import com.macgavrina.co_accounting.MainApplication
//import com.macgavrina.co_accounting.R
//import com.macgavrina.co_accounting.adapters.DebtsRecyclerViewAdapter
//import com.macgavrina.co_accounting.interfaces.DebtsContract
//import com.macgavrina.co_accounting.presenters.DebtsPresenter
//import com.macgavrina.co_accounting.room.Debt
//import kotlinx.android.synthetic.main.debts_fragment.*
//import com.google.android.material.snackbar.Snackbar
//import com.macgavrina.co_accounting.logging.Log
//import io.reactivex.disposables.Disposable
//
//
//class DebtsFragment: Fragment(), DebtsContract.View {
//
//    private var subscriptionToBus: Disposable? = null
//    lateinit var presenter: DebtsPresenter
//    private lateinit var viewManager: RecyclerView.LayoutManager
//    private var debtsList = mutableListOf<Debt>()
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//
//        presenter = DebtsPresenter()
//        presenter.attachView(this)
//
//        return inflater.inflate(R.layout.debts_fragment, container,
//                false)
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        debts_fragment_add_fab.setOnClickListener { view ->
//            presenter.addDebtButtonIsPressed()
//        }
//
//        viewManager = LinearLayoutManager(MainApplication.applicationContext())
//        debts_fragment_recyclerview.adapter = DebtsRecyclerViewAdapter(debtsList)
//        debts_fragment_recyclerview.layoutManager = viewManager
//    }
//
//    override fun onResume() {
//        super.onResume()
//
////        debts_fragment_recyclerview.addItemDecoration(DividerItemDecoration(context!!,
////                DividerItemDecoration.VERTICAL))
//        presenter.viewIsReady()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        presenter.detachView()
//    }
//
//    override fun displayToast(text: String) {
//        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
//    }
//
//    override fun displayRevertChangesAction() {
//    }
//
//    override fun initializeList(inputDebtsList: List<Debt>) {
//        debtsList.clear()
//        debtsList.addAll(inputDebtsList)
//
//        if (debtsList.isEmpty()) {
//            debts_fragment_empty_list_layout.visibility = View.VISIBLE
//        } else {
//            debts_fragment_empty_list_layout.visibility = View.INVISIBLE
//            debts_fragment_recyclerview.adapter?.notifyDataSetChanged()
//        }
//    }
//
//    override fun updateList() {
//    }
//
//    override fun showProgress() {
//        debts_fragment_progress_bar.visibility = View.VISIBLE
//    }
//
//    override fun hideProgress() {
//        debts_fragment_progress_bar.visibility = View.INVISIBLE
//    }
//
//
//    override fun displayOnDeleteDebtSnackBar() {
//
//        val snackBar = Snackbar.make(debts_fragment_const_layout, "Debt is deleted", Snackbar.LENGTH_LONG)
//        snackBar!!.setAction("Undo") {
//            Log.d("snackBar: undo action is pressed")
//            snackBar?.dismiss()
////            if (main_webview_fragment_webview.canGoBack()) {
////                main_webview_fragment_webview.goBack()
////            } else {
////                main_webview_fragment_webview.loadUrl(MAIN_URL)
////            }
//            presenter.undoDeleteDebtButtonIsPressed()
//        }
//        snackBar?.show()
//    }
//
//}