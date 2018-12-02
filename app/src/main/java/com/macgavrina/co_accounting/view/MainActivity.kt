package com.macgavrina.co_accounting.view

import android.accounts.Account
import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.presenters.MainActivityPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import android.content.Intent




class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainActivityContract.View {

    lateinit var presenter: MainActivityPresenter
    lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        presenter = MainActivityPresenter()
        presenter.attachView(this)

        //account = CreateSyncAccount(this)!!

/*        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/

        nav_view.getHeaderView(0).nav_header_main_iv.setOnClickListener {view ->
            presenter.headerIsClicked()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        initView()

        nav_view.menu.getItem(2).isChecked = true
        presenter.gotoDebts()
    }

    override fun onResume() {
        super.onResume()
        presenter.viewIsReady()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_contacts -> {
                presenter.gotoContactsEvent()
            }
            R.id.nav_debts -> {
                presenter.gotoDebts()
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    fun initView() {
        presenter.viewIsCreated()
    }

    override fun updateLoginText(login: String) {
        nav_view.getHeaderView(0).nav_header_main_tv.text = login
    }

    override fun displayProfileFragment() {
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, ProfileFragment())
                .addToBackStack("ProfileFragment")
                .commit()
    }

    override fun displayContactsFragment() {
        clearStack()
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, ContactsFragment())
                .addToBackStack("ContactsFragment")
                .commit()
    }

    override fun displayRegisterFragment(enteredLogin: String?) {
        val supportFragmentManager = supportFragmentManager
        val registerFragment:RegisterFragment = RegisterFragment()
        val bundle:Bundle = Bundle()
        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
        Log.d("enteredLogin = ${enteredLogin}")
        registerFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, registerFragment)
                .addToBackStack("RegisterFragment")
                .commit()
    }

    override fun displayEditContactFragment(uid: String?) {
        val supportFragmentManager = supportFragmentManager
        val editContactFragment:EditContactFragment = EditContactFragment()
        val bundle:Bundle = Bundle()
        bundle.putString(EditContactFragment.CONTACT_UID_KEY, uid)
        editContactFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, editContactFragment)
                .addToBackStack("EditContactFragment")
                .commit()
    }

    override fun displayDebtsFragment() {
        Log.d("display debts fragment")
        clearStack()
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, DebtsFragment())
                .addToBackStack("DebtsFragment")
                .commit()
    }

    override fun showProgress() {
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, ProgressBarFragment())
                .commit()
    }

    override fun hideProgress() {
        clearStack()
        displayMainFragment()
    }

    override fun hideMenu() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    override fun displayLoginFragment(enteredLogin: String?) {

        val loginFragment = LoginFragment()
        val bundle:Bundle = Bundle()
        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
        loginFragment.arguments = bundle

        clearStack()

        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, loginFragment)
                .addToBackStack("LoginFragment")
                .commit()
    }

    override fun displayRecoverPassFragment(enteredLogin: String?) {

        val recoverPasswordFragment = RecoverPasswordFragment()
        val bundle:Bundle = Bundle()
        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
        recoverPasswordFragment.arguments = bundle

        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, recoverPasswordFragment)
                .addToBackStack("RecoverPasswordFragment")
                .commit()
    }

    override fun displayRecoverPassSuccessDialog(title: String, text: String, enteredLogin: String?) {
        clearStack()
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(text)
                .setTitle(title)
        alertDialogBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, id ->
            Log.d("ok button")
            displayLoginFragment(enteredLogin)
        })
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun displayRegisterSuccessDialog(title: String, text: String) {
        clearStack()
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(text)
                .setTitle(title)
        alertDialogBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, id ->
            Log.d("ok button")
            displayMainFragment()
        })
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun displayMainFragment() {
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, MainFragment())
                .commit()
    }

    override fun displayAddContactFragment() {
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, AddContactFragment())
                .addToBackStack("AddContactFragment")
                .commit()
    }

    override fun displayAddDebtFragment(debtId: String?) {

//        val addDebtFragment = AddDebtFragment()
//
//        if (debtId != null) {
//            val bundle = Bundle()
//            bundle.putInt(AddReceiverInAddDebtFragment.DEBT_ID_KEY, debtId.toInt())
//            addDebtFragment.arguments = bundle
//        }
//
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.content_main_constraint_layout, addDebtFragment)
//                .addToBackStack("AddDebtFragment")
//                .commit()


        val intent = Intent()
        intent.action = "com.macgavrina.indebt.DEBT"
        if (debtId == null) {
            intent.putExtra("debtId", -1)
        } else {
            intent.putExtra("debtId", debtId?.toInt())
        }
        startActivity(intent)
    }

    override fun displayAddReceiverInAddDebtFragment(debtId: Int, expenseId: Int?) {
//        val addReceiverInAddDebtFragment = ExpenseActivity()
//        val bundle = Bundle()
//        bundle.putInt("debtId", debtId)
//        if (expenseId != null) {
//            bundle.putInt("expenseIdKey", expenseId)
//        }
//        addReceiverInAddDebtFragment.arguments = bundle
//
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.content_main_constraint_layout, addReceiverInAddDebtFragment)
//                .addToBackStack("AddReceiverInDebtFragment")
//                .commit()
    }

    override fun dismissAddReceiverInAddDebtFragment() {
        this.onBackPressed()
    }

    override fun displayAddReceiverInAddDebtFragmentAfterReceiverAdded() {
        this.onBackPressed()
        presenter.addReceiverInAddDebtFragmentAfterReceiverAddedIsDisplayed()
    }

    private fun clearStack() {
        val supportFragmentManager = supportFragmentManager
        var count = supportFragmentManager.getBackStackEntryCount()
        while (count > 0) {
            supportFragmentManager.popBackStack()
            count--
        }
    }

    /*
    fun CreateSyncAccount(context: Context): Account? {
        // Create the account type and default account
        val newAccount = Account(SyncService.ACCOUNT, SyncService.ACCOUNT_TYPE)
        // Get an instance of the Android account manager
        val accountManager = context.getSystemService(
                Context.ACCOUNT_SERVICE) as AccountManager
        *//*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         *//*
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            *//*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             *//*
            Log.d("account is initialized")
            return newAccount
        } else {
            *//*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             *//*
            Log.d("account initializing error")
            return newAccount
        }
    }*/

    override fun displayToast(text:String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }
}
