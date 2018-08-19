package com.macgavrina.co_accounting.view

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.presenters.LoginPresenter
import com.macgavrina.co_accounting.presenters.MainActivityPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.android.synthetic.main.nav_header_main.view.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainActivityContract.View {

    lateinit var presenter: MainActivityPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initView()

        presenter = MainActivityPresenter()
        presenter.attachView(this)

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
        //ToDo Здесь должно быть DI или что-то типа того, например:
        //ButterKnife.bind(this);
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

    private fun clearStack() {
        val supportFragmentManager = supportFragmentManager
        var count = supportFragmentManager.getBackStackEntryCount()
        while (count > 0) {
            supportFragmentManager.popBackStack()
            count--
        }
    }
}
