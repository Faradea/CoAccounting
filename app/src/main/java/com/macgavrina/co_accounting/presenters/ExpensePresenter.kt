package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDB
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.MoneyFormatter
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class ExpensePresenter: BasePresenter<AddReceiverInAddDebtContract.View>(), AddReceiverInAddDebtContract.Presenter {

    private var subscriptionToBus: Disposable? = null

    var debtId: Int? = null
    var expenseId: Int? = null
    var expense: Expense? = null
    var amountPerPerson: Double = 0.0
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
                                Log.d("Catch Events.NewContactIsAddedToSelectedReceiversList event")
                                val contact = `object`.myContact
                                notSelectedContactsList.remove(contact)
                                selectedContactsList.add(contact!!)

                                amountPerPerson = (getView()?.getAmount() ?:0.0) / selectedContactsList.size
                                Log.d("amountPerPerson = $amountPerPerson, totalAmount = ${getView()?.getAmount()}, selectedContactsList.size = ${selectedContactsList.size}")

                                getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
                                getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
                            }
                            is Events.OnClickSelectedReceiverOnAddExpenseFragment -> {
                                Log.d("Catch Events.OnClickSelectedReceiverOnAddExpenseFragment event")
                                val contact = `object`.myContact
                                selectedContactsList.remove(contact)
                                notSelectedContactsList.add(contact)

                                amountPerPerson = (getView()?.getAmount() ?:0.0) / selectedContactsList.size

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
        Log.d("DebtId is received from MainActivity, = $debtId")
        this.debtId = debtId
    }

    override fun expenseIdIsReceivedFromMainActivity(expenseId: Int) {

        if (expenseId == -1) return

        Log.d("ExpenseId is received from MainActivity, = $expenseId, getting expense data from DB...")
        this.expenseId = expenseId

        MainApplication.db.expenseDAO().getExpenseByIds(expenseId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Expense>() {
                    override fun onSuccess(expense: Expense) {
                        Log.d("Expense is received from DB, = $expense")
                        this@ExpensePresenter.expense = expense

                        getView()?.showDeleteButton()

                        getView()?.setAmount(expense.totalAmount)

                        getView()?.setComment(expense.comment)

                        Log.d("Getting receivers with amount for expense...")
                        MainApplication.db.receiverWithAmountForDBDAO().getReceiversWithAmountForExpense(expense.uid.toString())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : DisposableMaybeObserver<List<ReceiverWithAmountForDB>>() {
                                    override fun onSuccess(receiversWithAmountList: List<ReceiverWithAmountForDB>) {
                                        Log.d("Receivers with amount are received from DB, size = ${receiversWithAmountList.size}")
                                        this@ExpensePresenter.receiversWithAmountList = receiversWithAmountList as MutableList<ReceiverWithAmountForDB>
                                        receiversWithAmountListIsLoaded = true
                                        updateReceiverWithAmountListWithDataFromDB()
                                    }

                                    override fun onError(e: Throwable) {
                                        Log.d("Error getting receivers with amount from DB, $e")
                                    }

                                    override fun onComplete() {
                                    }
                                })
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting expense from DB, $e")
                    }

                    override fun onComplete() {
                        Log.d("No expense with specified id in DB")
                    }
                })
    }

    override fun amountIsEdited(newAmount: Double) {
        Log.d("Amount is edited, new value = $newAmount")
        if (selectedContactsList.isNotEmpty()) {
            amountPerPerson = newAmount/selectedContactsList.size
            getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
        }

    }

    override fun viewIsReady() {
        getAndDisplayAllContacts()
        getView()?.hideDeleteButton()
    }

    override fun cancelButtonInToolbarIsClicked() {
        Log.d("Cancel button in toolbar is clicked")
        getView()?.hideKeyboard()
        MainApplication.bus.send(Events.HideAddReceiverInAddDebtFragment(false))
    }

    override fun saveButtonIsPressed() {
        Log.d("Save button is pressed")

        getView()?.hideKeyboard()

        var receiversListString = ""

        receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

        selectedContactsList.forEach { contact ->

            val receiverWithAmount = ReceiverWithAmountForDB()
            receiverWithAmount.amount = amountPerPerson
            receiverWithAmount.contactId = contact.uid
            receiversWithAmountList.add(receiverWithAmount)

            if (receiversListString == "") {
                receiversListString = contact.alias.toString()
            } else {
                receiversListString = "$receiversListString, ${contact.alias.toString()}"
            }
        }


        if (expense == null) {

            if (selectedContactsList.isEmpty()) {
                getView()?.showAlertAndFinishSelf("Selected contacts list is empty, expense won't be saved")
                return
            }
            expense = Expense()
            expense!!.totalAmount = getView()?.getAmount() ?: 0.0
            expense!!.comment = getView()?.getComment() ?: ""
            expense!!.debtId = debtId
            expense!!.isForExpertMode = true

            Log.d("Adding new expense to DB, expense = $expense")

            Completable.fromAction {
                MainApplication.db.expenseDAO().insertExpense(expense!!)
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {

                        override fun onSubscribe(d: Disposable) {}

                        override fun onError(e: Throwable) {
                            Log.d("Error adding expense to DB, $e")
                        }

                        override fun onComplete() {
                            Log.d("Expense is added to DB, getting it's id...")
                            MainApplication.db.expenseDAO().getLastExpenseId()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : DisposableMaybeObserver<Int>() {
                                        override fun onSuccess(uid: Int) {
                                            receiversWithAmountList?.forEach { receiversWithAmount ->
                                                receiversWithAmount.expenseId = uid.toString()
                                                receiversWithAmount.amount = MoneyFormatter.justRound(receiversWithAmount.amount)
                                            }

                                            Log.d("Saving receiverWithAmountList for expenseId = $uid")


                                            Completable.fromAction {
                                                MainApplication.db.receiverWithAmountForDBDAO().insertAll(*receiversWithAmountList!!.toTypedArray())
                                            }.observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                                                        override fun onSubscribe(d: Disposable) {}

                                                        override fun onComplete() {
                                                            Log.d("Receiver with amount list is saved in DB")
                                                            getView()?.finishSelf()
                                                        }

                                                        override fun onError(e: Throwable) {
                                                            Log.d("Error adding receivers with amount for DB, $e")
                                                        }
                                                    })
                                        }

                                        override fun onError(e: Throwable) {
                                            Log.d("Error getting added expense id from DB, $e")
                                        }

                                        override fun onComplete() {
                                        }
                                    })
                        }
                    })

        } else {
            if (selectedContactsList.isEmpty()) {
                getView()?.showAlertAndFinishSelfWithCallback("Selected contacts list is empty, expense will be deleted")
                return
            }
            expense!!.totalAmount = MoneyFormatter.justRound(getView()?.getAmount() ?: 0.0)
            expense!!.comment = getView()?.getComment() ?: ""
            expense!!.debtId = debtId
            expense!!.isForExpertMode = true

            Log.d("Updating existing expense in DB, $expense")
            Completable.fromAction {
                MainApplication.db.expenseDAO().updateExpense(expense!!)
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {

                        override fun onSubscribe(d: Disposable) {}

                        override fun onError(e: Throwable) {
                            Log.d("Error updating expense, $e")
                        }

                        override fun onComplete() {
                            Log.d("Expense is updated, deleting old receivers with amount for this expense...")

                            val subscription = MainApplication.db.receiverWithAmountForDBDAO().deleteReceiversWithAmountForExpense(expense?.uid.toString())
                                .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe ({ numberOfDeleteRows ->
                                        Log.d("Old receivers with amount are deleted, number = $numberOfDeleteRows")
                                        receiversWithAmountList?.forEach { receiversWithAmount ->
                                            receiversWithAmount.expenseId = expense?.uid.toString()
                                            receiversWithAmount.amount = MoneyFormatter.justRound(receiversWithAmount.amount)
                                        }

                                        Log.d("Adding new list, size = ${receiversWithAmountList.size}")
                                        Completable.fromAction {
                                            MainApplication.db.receiverWithAmountForDBDAO().insertAll(*receiversWithAmountList!!.toTypedArray())
                                        }.observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                                                    override fun onSubscribe(d: Disposable) {}

                                                    override fun onComplete() {
                                                        Log.d("New receivers with amount for updated expense are added")
                                                        getView()?.finishSelf()
                                                    }

                                                    override fun onError(e: Throwable) {
                                                        Log.d("Error adding new receivers with amount for expense, $e")
                                                    }
                                                })
                                    }, {e ->
                                        Log.d("Error deleting old receiver with amount for expense, $e")
                                    })
                        }
                    })
        }
    }

    override fun userHasReadAlertAboutDeletingExpense() {
        deleteCurrentExpense()
        getView()?.finishSelf()
    }
    override fun deleteButtonIsPressed() {

        Log.d("Delete button is pressed, deleting expense from DB...")

        deleteCurrentExpense()
    }

    private fun updateReceiverWithAmountListWithDataFromDB() {

        if (receiversWithAmountListIsLoaded && contactsListIsLoaded) {

            receiversWithAmountList.forEach { receiversWithAmount ->

                val contactId = receiversWithAmount.contactId
                val contact: Contact? = contactsListToIdMap!![contactId.toString()]
                if (contact != null) {
                    selectedContactsList.add(contact!!)
                    notSelectedContactsList.remove(contact)
                }
            }

            amountPerPerson = (getView()?.getAmount() ?:0.0) / selectedContactsList.size

            getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
            getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)

        }
    }

    private fun getAndDisplayAllContacts() {

        Log.d("Getting all contacts from DB...")
        MainApplication.db.contactDAO().getActiveContactsForCurrentTripRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<com.macgavrina.co_accounting.room.Contact>>() {
                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
                        Log.d("Contacts are received, size = ${contactsList.size}")
                        this@ExpensePresenter.contactsList = contactsList
                        amountPerPerson = 0.0
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
                        Log.d("Error receiving contacts, $e")
                    }

                    override fun onComplete() {
                        Log.d("No contacts in DB")
                    }
                })
    }

    private fun deleteCurrentExpense() {
        Completable.fromAction {
            MainApplication.db.expenseDAO().deleteExpense(expense!!)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {

                    override fun onSubscribe(d: Disposable) {}

                    override fun onError(e: Throwable) {
                        Log.d("Error deleting expense, $e")
                    }

                    override fun onComplete() {
                        Log.d("Expense is deleted")
                        getView()?.finishSelf()
                        //MainApplication.bus.send(Events.HideAddReceiverInAddDebtFragment(true))
                    }
                })
    }
}