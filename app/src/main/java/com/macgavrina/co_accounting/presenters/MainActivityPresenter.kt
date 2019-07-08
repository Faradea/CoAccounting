//package com.macgavrina.co_accounting.presenters
//
//import com.macgavrina.co_accounting.MainApplication
//import com.macgavrina.co_accounting.R
//import com.macgavrina.co_accounting.logging.Log
//import com.macgavrina.co_accounting.room.Contact
//import com.macgavrina.co_accounting.model.User
//import com.macgavrina.co_accounting.providers.UserProvider
//import com.macgavrina.co_accounting.rxjava.Events
//import com.macgavrina.co_accounting.support.STATUS_ACTIVE
//import io.reactivex.Completable
//import io.reactivex.CompletableObserver
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.disposables.Disposable
//import io.reactivex.functions.Action
//import io.reactivex.schedulers.Schedulers
//
//
//class MainActivityPresenter:BasePresenter<MainActivityContract.View>(), MainActivityContract.Presenter, UserProvider.LoadUserCallback, UserProvider.CheckIfUserTokenExistCallback {
//
//    private var lastDeletedContact: Contact? = null
//    private var subscriptionToBus: Disposable? = null
//
//    private fun subscribeToEventBus() {
//        if (subscriptionToBus == null) {
//            subscriptionToBus =
////                            is Events.ContactCannotBeDisableForTrip -> {
////                                Log.d("Catch Events.ContactCannotBeDisableForTrip event")
////                                getView()?.displayAlert("Contact can't be disabled for trip until it used for debts", "Contact can't be disabled for trip")
////                            }
////                        is Events.OnClickExpenseItemList -> {
////                            getView()?.displayAddReceiverInAddDebtFragment(`object`.myDebtId, `object`.myExpenseId)
////                        }
//                        }
//                    }
//        }
//    }
//
//
//    override fun onLoad(user:User) {
//        //Выполняется после получения callback с данными о пользователе от UserProvider.CheckIfUserTokenExistCallback
//        if (user.login.length != 0) {
//            getView()?.updateLoginText(user.login)
//        }
//        else {
//            getView()?.updateLoginText(MainApplication.applicationContext().getString(R.string.default_user_name))
//        }
//    }
//
//    //Выполняется после получения callback с данными о пользователе от UserProvider.loadUser
//    override fun onLoad(ifExist:Boolean) {
//            if (ifExist) {
//                Log.d("User is already loggined")
//                getView()?.hideProgress()
//                getView()?.displayProfileFragment()
//            } else {
//                getView()?.hideProgress()
//                getView()?.displayLoginFragment(null)
//            }
//    }
//
//    override fun undoDeleteContactButtonIsPressed() {
//        Log.d("Undo delete contact button is pressed, updating contact status...")
//        if (lastDeletedContact == null) return
//
//        lastDeletedContact!!.status = STATUS_ACTIVE
//        Completable.fromAction(object : Action {
//            @Throws(Exception::class)
//            override fun run() {
//                MainApplication.db.contactDAO().updateContact(lastDeletedContact!!)
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : CompletableObserver {
//            override fun onSubscribe(d: Disposable) {
//
//            }
//
//            override fun onComplete() {
//                Log.d("Contact is restored")
//                lastDeletedContact = null
//                MainApplication.bus.send(Events.DeletedContactIsRestored())
//            }
//
//            override fun onError(e: Throwable) {
//                Log.d("Error restoring contact, $e")
//            }
//        })
//    }
//}