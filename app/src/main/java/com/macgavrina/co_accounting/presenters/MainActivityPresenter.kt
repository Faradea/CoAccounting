package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.DateFormatter
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers


class MainActivityPresenter:BasePresenter<MainActivityContract.View>(), MainActivityContract.Presenter, UserProvider.LoadUserCallback, UserProvider.CheckIfUserTokenExistCallback {

    private var lastDeletedContact: Contact? = null
    private var subscriptionToBus: Disposable? = null
    private var dataToShare: String = ""

    override fun attachView(baseViewContract: MainActivityContract.View) {
        super.attachView(baseViewContract)

        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()

        unsubscribeFromEventBus()
    }

    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.FromRegisterToLoginEvent -> {
                                getView()?.displayLoginFragment(`object`.myEnteredLogin)
                            }
                            is Events.RegisterIsSuccessful -> {
                                UserProvider().loadUser(this)
                                getView()?.displayRegisterSuccessDialog(`object`.myTitle, `object`.myText)
                            }
                            is Events.RecoverPassIsSuccessful -> {
                                getView()?.displayRecoverPassSuccessDialog(`object`.myTitle, `object`.myText, `object`.myEnteredLogin)
                            }
                            is Events.LogoutFinished -> {
                                UserProvider().loadUser(this)
                                getView()?.displayLoginFragment(null)
                            }
                            is Events.LoginIsSuccessful -> {
                                UserProvider().loadUser(this)
                                getView()?.displayMainFragment()
                            }
                            is Events.FromLoginToRegister -> {
                                getView()?.displayRegisterFragment(`object`.myEnteredLogin)
                            }
                            is Events.FromLoginToRecoverPass -> {
                                getView()?.displayRecoverPassFragment(`object`.myEnteredLogin)
                            }
                            is Events.AddContact -> {
                                getView()?.displayAddContactFragment(null)
                            }
                            is Events.ContactIsAdded -> {
                                getView()?.displayContactsFragment()
                            }
                            is Events.OnClickContactList -> {
                                getView()?.displayEditContactFragment(`object`.myUid)
                            }
                            is Events.ContactEditingIsFinished -> {
                                getView()?.displayContactsFragment()
                            }
                            is Events.ContactIsDeleted -> {
                                lastDeletedContact = `object`.contact
                                getView()?.displayOnDeleteContactSnackBar()
                            }
                            is Events.AddDebt -> {
                                getView()?.displayAddDebtFragment(null)
                            }
                            is Events.DebtIsAdded -> {
                                getView()?.displayDebtsFragment()
                            }
                            is Events.AddReceiverButtonInAddDebtFragment -> {
                                getView()?.displayAddReceiverInAddDebtFragment(`object`.myUid, null)
                            }
                            is Events.HideAddReceiverInAddDebtFragment -> {
                                if (`object`.myWithSaveChanges == true) {
                                    getView()?.displayToast("Expense is saved")
                                }
                                getView()?.dismissAddReceiverInAddDebtFragment()
                            }
                            is Events.ReceiversWithAmountInAddDebtIsSaved -> {
                                getView()?.displayAddReceiverInAddDebtFragmentAfterReceiverAdded()
                            }
                            is Events.OnClickDebtItemList -> {
                                getView()?.displayAddDebtFragment(`object`.myUid)
                            }
//                        is Events.OnClickExpenseItemList -> {
//                            getView()?.displayAddReceiverInAddDebtFragment(`object`.myDebtId, `object`.myExpenseId)
//                        }
                        }
                    }
        }
    }

    private fun unsubscribeFromEventBus() {
        if (subscriptionToBus != null) {
            subscriptionToBus?.dispose()
            subscriptionToBus = null
        }
    }

    override fun gotoContactsEvent() {
        getView()?.displayContactsFragment()
    }

    override fun gotoDebts() {
        getView()?.displayDebtsFragment()
    }

    override fun viewIsReady() {
        Log.d("MainActivity view id ready")
        UserProvider().loadUser(this)
    }

    override fun viewIsCreated() {
        super.viewIsCreated()
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

    override fun addReceiverInAddDebtFragmentAfterReceiverAddedIsDisplayed() {
        MainApplication.bus.send(Events.AddDebtFragmentRequiresRefresh())
    }

    override fun undoDeleteContactButtonIsPressed() {
        if (lastDeletedContact == null) return

        lastDeletedContact!!.status = "active"
        Completable.fromAction(object : Action {
            @Throws(Exception::class)
            override fun run() {
                MainApplication.db.contactDAO().updateContact(lastDeletedContact!!)
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                lastDeletedContact = null
                MainApplication.bus.send(Events.DeletedContactIsRestored())
            }

            override fun onError(e: Throwable) {
            }
        })
    }


    override fun prepareAndShareData() {

        MainApplication.db.contactDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Contact>>() {
                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
                        dataToShare = dataToShare + "Contacts:" + "\n"

                        contactsList.forEach { contact ->
                            dataToShare = dataToShare + "\n" +"uid: ${contact.uid}, email: ${contact.email}, alias: ${contact.alias}, status: ${contact.status}"
                        }
                        dataToShare = dataToShare + "\n" + "\n"

                        loadDebts()
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                        getView()?.displayToast("Database error")
                    }

                    override fun onComplete() {
                    }
                })
    }

    private fun loadDebts() {

        MainApplication.db.debtDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Debt>>() {
                    override fun onSuccess(debtList: List<Debt>) {
                        Log.d("success")
                        dataToShare = dataToShare + "Debts:" + "\n"

                        debtList.forEach { debt ->
                            dataToShare = dataToShare + "\n" +"uid: ${debt.uid}, senderId:${debt.senderId}, datetime:${DateFormatter().formatDateFromTimestamp(debt.datetime!!.toLong())}, amount: ${debt.spentAmount}, comment:${debt.comment}, status:${debt.status}"
                        }

                        dataToShare = dataToShare + "\n" + "\n"

                        loadExpenses()

                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        //ToDo REFACT call dispose() here and in all onComplete
                        Log.d("nothing")
                    }
                })
    }

    private fun loadExpenses() {

        MainApplication.db.expenseDAO().getAll
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Expense>>() {
                    override fun onSuccess(expenseList: List<Expense>) {
                        dataToShare = dataToShare + "Expenses:" + "\n"

                        expenseList.forEach { expense ->
                            dataToShare = dataToShare + "\n" +"uid: ${expense.uid}, totalAmount:${expense.totalAmount}, debtId:${expense.debtId}, receiverList:${expense.receiversList}"
                        }

                        getView()?.startActivityToShareAllData(dataToShare)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }
}