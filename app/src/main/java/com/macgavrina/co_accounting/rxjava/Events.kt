package com.macgavrina.co_accounting.rxjava

import com.macgavrina.co_accounting.presenters.LoginPresenter

object Events {

    class FromRegisterToLoginEvent(enteredLogin: String?) {
        val myEnteredLogin = enteredLogin
    }

    class RegisterIsSuccessful(title: String, text: String) {
        val myTitle = title
        val myText = text
    }

    class RecoverPassIsSuccessful(title: String, text: String, enteredLogin: String?) {
        val myTitle = title
        val myText = text
        val myEnteredLogin = enteredLogin
    }

    class LogoutFinished{}

    class LoginIsSuccessful{}

    class FromLoginToRecoverPass(enteredLogin: String?){
        val myEnteredLogin = enteredLogin
    }

    class FromLoginToRegister(enteredLogin: String?){
        val myEnteredLogin = enteredLogin
    }

    class AddContact{}

    class ContactIsAdded{}

}