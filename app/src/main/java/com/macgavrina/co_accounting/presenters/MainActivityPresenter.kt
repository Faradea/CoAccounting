package com.macgavrina.co_accounting.presenters

import android.util.Log
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider


class MainActivityPresenter:BasePresenter<MainActivityContract.View>(), MainActivityContract.Presenter, UserProvider.LoadUserCallback, UserProvider.CheckIfUserTokenExistCallback {

    override fun onLoad(user:User) {
        Log.d("InDebtApp", "Login is loaded")
        if (user.login.length != 0) {
            getView()?.updateLoginText(user.login)
        }
        else {
            getView()?.updateLoginText("Anonymous")
        }

    }

    override fun logoutFinished() {
        UserProvider().loadUser(this)
        getView()?.displayLoginFragment()
    }

    override fun loginFinished() {
        UserProvider().loadUser(this)
        getView()?.displayMainFragment()
    }

    override fun viewIsReady() {
        Log.d("InDebtApp", "MainActivity view id ready")
        UserProvider().loadUser(this)
    }

    override fun headerIsClicked() {

        getView()?.hideMenu()

        getView()?.showProgress()
        UserProvider().checkIfUserTokenExist(this)

    }

    //Выполняется после получения callback с данными о пользователе от UserProvider
    override fun onLoad(ifExist:Boolean) {
            if (ifExist) {
                Log.d("InDebtApp", "User is already logined")
                getView()!!.displayProfileFragment()
            } else {
                getView()!!.displayLoginFragment()
            }
    }
}