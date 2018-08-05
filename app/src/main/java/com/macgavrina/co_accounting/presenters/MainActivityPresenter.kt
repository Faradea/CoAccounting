package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider


class MainActivityPresenter:BasePresenter<MainActivityContract.View>(), MainActivityContract.Presenter, UserProvider.LoadUserCallback, UserProvider.CheckIfUserTokenExistCallback {
    override fun passRecoverIsSuccessfull() {
        getView()?.displayDialog("Instruction has been sent to your email")
    }

    override fun onLoad(user:User) {
        Log.d("Login is loaded")
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

    override fun loginFinished(nextFragment: LoginPresenter.nextFragment) {
        UserProvider().loadUser(this)
        when (nextFragment) {
            LoginPresenter.nextFragment.MAIN ->
                getView()?.displayMainFragment()
            LoginPresenter.nextFragment.RECOVER_PASS ->
                getView()?.displayRecoverPassFragment()
        }

    }

    override fun viewIsReady() {
        Log.d("MainActivity view id ready")
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
                Log.d("User is already loggined")
                getView()!!.displayProfileFragment()
            } else {
                getView()!!.displayLoginFragment()
            }
    }
}