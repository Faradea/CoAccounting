package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider


class MainActivityPresenter:BasePresenter<MainActivityContract.View>(), MainActivityContract.Presenter, UserProvider.LoadUserCallback, UserProvider.CheckIfUserTokenExistCallback {

    override fun passRecoverIsSuccessfull(title: String, text: String, enteredLogin: String?) {
        getView()?.displayRecoverPassSuccessDialog(title, text, enteredLogin)
    }

    override fun registrationIsSuccessfull(title: String, text: String) {
        UserProvider().loadUser(this)
        getView()?.displayRegisterSuccessDialog(title, text)
    }

    override fun gotoLoginEvent(enteredLogin: String?) {
        getView()?.displayLoginFragment(enteredLogin)
    }

    override fun logoutFinished() {
        UserProvider().loadUser(this)
        getView()?.displayLoginFragment(null)
    }

    override fun loginFinished(nextFragment: LoginPresenter.nextFragment, enteredLogin: String?) {
        UserProvider().loadUser(this)
        when (nextFragment) {
            LoginPresenter.nextFragment.MAIN -> {
                getView()?.displayMainFragment()
            }

            LoginPresenter.nextFragment.RECOVER_PASS -> {
                getView()?.displayRecoverPassFragment(enteredLogin)
            }

            LoginPresenter.nextFragment.REGISTER -> {
                getView()?.displayRegisterFragment(enteredLogin)
            }

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

    override fun onLoad(user:User) {
        //Выполняется после получения callback с данными о пользователе от UserProvider.CheckIfUserTokenExistCallback
        if (user.login.length != 0) {
            getView()?.updateLoginText(user.login)
        }
        else {
            getView()?.updateLoginText(MainApplication.applicationContext().getString(R.string.default_user_name))
        }
    }

    //Выполняется после получения callback с данными о пользователе от UserProvider.loadUser
    override fun onLoad(ifExist:Boolean) {
            if (ifExist) {
                Log.d("User is already loggined")
                getView()?.hideProgress()
                getView()?.displayProfileFragment()
            } else {
                getView()?.hideProgress()
                getView()?.displayLoginFragment(null)
            }
    }
}