package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDB
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat

class ExpensePresenter: BasePresenter<AddReceiverInAddDebtContract.View>(), AddReceiverInAddDebtContract.Presenter {

    private var subscriptionToBus: Disposable? = null

    var debtId: Int? = null
    var expenseId: Int? = null
    var expense: Expense? = null
    var amountPerPerson: String = "0"
    var contactsList: List<Contact>? = null
    var contactsListToIdMap: MutableMap<String, Contact>? = null
    var notSelectedContactsList = mutableListOf<Contact>()
    var selectedContactsList = mutableListOf<Contact>()
    var receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

    var receiversWithAmountListIsLoaded = false
    var contactsListIsLoaded = false

    override fun attachView(baseViewContract: AddReceiverInAddDebtContract.View) {
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
                            is Events.NewContactIsAddedToSelectedReceiversList -> {
                                val contact = `object`.myContact
                                notSelectedContactsList.remove(contact)
                                selectedContactsList.add(contact!!)

                                if (getView()?.getAmount() != null) {
                                    amountPerPerson = DecimalFormat("##.##").format(getView()?.getAmount()!! / selectedContactsList.size)
                                } else {
                                    amountPerPerson = "0"
                                }
                                getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
                                getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
                            }
                            is Events.onClickSelectedReceiverOnAddExpenseFragment -> {
                                val contact = `object`.myContact
                                selectedContactsList.remove(contact)
                                notSelectedContactsList.add(contact)

                                if (getView()?.getAmount() != null) {
                                    amountPerPerson = DecimalFormat("##.##").format(getView()?.getAmount()!! / selectedContactsList.size)
                                } else {
                                    amountPerPerson = "0"
                                }
                                getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
                                getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)

                            }
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

    override fun debtIdIsReceiverFromMainActivity(debtId: Int) {
        this.debtId = debtId
    }

    override fun expenseIdIsReceivedFromMainActivity(expenseId: Int) {
        this.expenseId = expenseId

        MainApplication.db.expenseDAO().getExpenseByIds(expenseId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Expense>() {
                    override fun onSuccess(expense: Expense) {
                        this@ExpensePresenter.expense = expense

                        getView()?.showDeleteButton()

                        getView()?.setAmount(expense.totalAmount)

                        MainApplication.db.receiverWithAmountForDBDAO().getReceiversWithAmountForExpense(expense.uid.toString())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : DisposableMaybeObserver<List<ReceiverWithAmountForDB>>() {
                                    override fun onSuccess(receiversWithAmountList: List<ReceiverWithAmountForDB>) {
                                        Log.d("success")
                                        this@ExpensePresenter.receiversWithAmountList = receiversWithAmountList as MutableList<ReceiverWithAmountForDB>
                                        receiversWithAmountListIsLoaded = true
                                        updateReceiverWithAmountListWithDataFromDB()
                                    }

                                    override fun onError(e: Throwable) {
                                        Log.d("error, ${e.toString()}")
                                    }

                                    override fun onComplete() {
                                        Log.d("nothing")
                                    }
                                })
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("No expense with specified id in DB")
                    }
                })
    }

    override fun amountIsEdited(newAmount: Float) {
        if (selectedContactsList.isNotEmpty()) {
            amountPerPerson = DecimalFormat("##.##").format(newAmount/selectedContactsList.size)
            getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
        }

    }

    override fun viewIsReady() {

        getAndDisplayAllContacts()
        getView()?.hideDeleteButton()

    }

    override fun cancelButtonInToolbarIsClicked() {
        getView()?.hideKeyboard()
        MainApplication.bus.send(Events.HideAddReceiverInAddDebtFragment(false))
    }

    override fun saveButtonIsPressed() {

        getView()?.hideKeyboard()

        var receiversListString = ""

        receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

        selectedContactsList.forEach { contact ->

            val receiverWithAmount = ReceiverWithAmountForDB()
            receiverWithAmount.amount = amountPerPerson
            receiverWithAmount.contactId = contact.uid.toString()
            receiversWithAmountList.add(receiverWithAmount)

            if (receiversListString == "") {
                receiversListString = contact.alias.toString()
            } else {
                receiversListString = "$receiversListString, ${contact.alias.toString()}"
            }
        }


        if (expense == null) {
            expense = Expense()
            expense!!.totalAmount = DecimalFormat("##.##").format(getView()?.getAmount())
            expense!!.receiversList = receiversListString
            expense!!.debtId = debtId

            Log.d("add expense: debtId = ${expense!!.debtId}, receiversList = ${expense!!.receiversList}, amount = ${expense!!.totalAmount}")

            Completable.fromAction {
                MainApplication.db.expenseDAO().insertExpense(expense!!)
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {

                        override fun onSubscribe(d: Disposable) {}

                        override fun onError(e: Throwable) {
                            Log.d(e.toString())
                        }

                        override fun onComplete() {
                            MainApplication.db.expenseDAO().getLastExpenseId()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : DisposableMaybeObserver<Int>() {
                                        override fun onSuccess(uid: Int) {
                                            receiversWithAmountList?.forEach { receiversWithAmount ->
                                                receiversWithAmount.expenseId = uid.toString()
                                            }

                                            Log.d("saving receiverWithAmountList for expenseId = $uid")


                                            Completable.fromAction {
                                                MainApplication.db.receiverWithAmountForDBDAO().insertAll(*receiversWithAmountList!!.toTypedArray())
                                            }.observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                                                        override fun onSubscribe(d: Disposable) {}

                                                        override fun onComplete() {
                                                            Log.d("receiver with amount list is added")

                                                            getView()?.finishSelf()
                                                            //MainApplication.bus.send(Events.ReceiversWithAmountInAddDebtIsSaved())
                                                        }

                                                        override fun onError(e: Throwable) {
                                                            Log.d("Error adding receivers with amount for DB")
                                                        }
                                                    })
                                        }

                                        override fun onError(e: Throwable) {
                                            Log.d(e.toString())
                                        }

                                        override fun onComplete() {
                                            Log.d("nothing")
                                        }
                                    })
                        }
                    })

        } else {
            expense!!.totalAmount = DecimalFormat("##.##").format(getView()?.getAmount())
            expense!!.receiversList = receiversListString
            expense!!.debtId = debtId

            Completable.fromAction {
                MainApplication.db.expenseDAO().updateExpense(expense!!)
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {

                        override fun onSubscribe(d: Disposable) {}

                        override fun onError(e: Throwable) {
                            Log.d(e.toString())
                        }

                        override fun onComplete() {
                            Completable.fromAction {
                                MainApplication.db.receiverWithAmountForDBDAO().deleteReceiversWithAmountForExpense(expense?.uid.toString())
                            }.observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                                        override fun onSubscribe(d: Disposable) {}

                                        override fun onComplete() {
                                            receiversWithAmountList?.forEach { receiversWithAmount ->
                                                receiversWithAmount.expenseId = expense?.uid.toString()
                                            }

                                            Completable.fromAction {
                                                MainApplication.db.receiverWithAmountForDBDAO().insertAll(*receiversWithAmountList!!.toTypedArray())
                                            }.observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                                                        override fun onSubscribe(d: Disposable) {}

                                                        override fun onComplete() {
                                                            //ToDo Hide loader
                                                        }

                                                        override fun onError(e: Throwable) {
                                                            Log.d("$e")
                                                        }
                                                    })
                                        }

                                        override fun onError(e: Throwable) {
                                            Log.d("$e")
                                        }
                                    })
                        }
                    })
        }
    }

    override fun deleteButtonIsPressed() {

        Completable.fromAction {
            MainApplication.db.expenseDAO().deleteExpense(expense!!)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {

                    override fun onSubscribe(d: Disposable) {}

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        getView()?.finishSelf()
                        //MainApplication.bus.send(Events.HideAddReceiverInAddDebtFragment(true))
                    }
                })
    }

    private fun updateReceiverWithAmountListWithDataFromDB() {

        if (receiversWithAmountListIsLoaded && contactsListIsLoaded) {

            Log.d("go through receiversWithAmountList with ${receiversWithAmountList.size} items")
            receiversWithAmountList.forEach { receiversWithAmount ->

                val contactId = receiversWithAmount.contactId
                Log.d("go throw receiversWithAmountList, for contactId = $contactId")
                val contact = contactsListToIdMap!![contactId!!]
                selectedContactsList.add(contact!!)
                notSelectedContactsList.remove(contact)
            }

            if (getView()?.getAmount() != null) {
                amountPerPerson =  DecimalFormat("##.##").format(getView()?.getAmount()!! / selectedContactsList.size)

            } else {
                amountPerPerson = "0"
            }

            getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
            getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)

        }
    }

    private fun getAndDisplayAllContacts() {
        MainApplication.db.contactDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<com.macgavrina.co_accounting.room.Contact>>() {
                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
                        this@ExpensePresenter.contactsList = contactsList
                        amountPerPerson = "0"
                        notSelectedContactsList.clear()
                        selectedContactsList.clear()

                        contactsListToIdMap = mutableMapOf()
                        contactsList.forEach { contact ->
                            contactsListToIdMap!![contact.uid.toString()] = contact
                            notSelectedContactsList.add(contact)
                        }
                        getView()?.initializeNotSelectedReceiversList(contactsList)

                        contactsListIsLoaded = true
                        updateReceiverWithAmountListWithDataFromDB()
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("No contacts in DB")
                    }
                })
    }
}