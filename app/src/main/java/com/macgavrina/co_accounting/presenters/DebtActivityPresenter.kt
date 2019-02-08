package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.DebtActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.ReceiverWithAmount
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.DateFormatter
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class DebtActivityPresenter:BasePresenter<DebtActivityContract.View>(), DebtActivityContract.Presenter {

    private var subscriptionToBus: Disposable? = null

    var senderId: Int? = null
    var addDebtButtonEnabled: Boolean = false
    lateinit var debt: Debt
    lateinit var contactsIdToNameMap: MutableMap<String, Contact>
    lateinit var positionToContactIdMap: MutableMap<Int, Contact>
    lateinit var contactIdToPositionMap: MutableMap<Int, Int>
    lateinit var receiverWithAmountList: MutableList<ReceiverWithAmount>
    lateinit var friendsList: Array<String?>
    lateinit var currentTrip: Trip

    override fun attachView(baseViewContract: DebtActivityContract.View) {
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
                            is Events.AddDebtReceiverWithAmountListIsChanged -> {
                                val newAmount = `object`.myNewText
                                val positionInList = `object`.myPositionInList
                                Log.d("Catch Events.AddDebtReceiverWithAmountListIsChanged event, newAmount = $newAmount, position = $positionInList")
                                receiverWithAmountList[positionInList].amount = newAmount.toFloat()
                            }
                            is Events.OnClickExpenseItemList -> {
                                Log.d("Catch OnClickExpenseItemList event, debtId = ${`object`.myDebtId}, expenseId = ${`object`.myExpenseId}")
                                getView()?.displayExpenseActivity(`object`.myDebtId, `object`.myExpenseId)
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

    override fun viewIsReady() {

        getView()?.hideProgress()

        getAllContactsFromDB()

        if (::debt.isInitialized) {
            getAndDisplayExpensesForDebt(debt.uid.toString())
        }

        if (::friendsList.isInitialized && senderId != null) {

            if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[senderId!!]!=null) {
                getView()?.setSender(contactIdToPositionMap[senderId!!]!!)
            }
        }
    }

    override fun viewIsPaused() {
        senderId = positionToContactIdMap[getView()?.getSender()]?.uid
    }

    override fun viewIsCreated() {
        super.viewIsCreated()

        getView()?.hideDeleteButton()
        getView()?.hideClearButton()

        TripRepository(MainApplication.instance).getCurrentTrip()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({ maybeTrip ->
                    maybeTrip
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe ({ trip ->
                                Log.d("Current trip = $trip")
                                currentTrip = trip
                            }, {error ->
                                Log.d("Error getting current trip from DB, $error")
                                getView()?.displayToast("Database error")
                            })
                }, {error ->
                    Log.d("Error getting current trip from DB, $error")
                    getView()?.displayToast("Database error")
                })
    }

    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        addDebtButtonEnabled = areFilled
    }

    override fun doneButtonIsPressed() {

        Log.d("Done button is pressed")
        getView()?.hideKeyboard()
        getView()?.showProgress()

        debt.senderId = positionToContactIdMap[getView()?.getSender()]?.uid.toString()
        debt.spentAmount= getView()?.getAmount()

        if (getView()?.getDate() != null) {

            if (getView()?.getTime() == null) {
                val formattedDate = DateFormatter().getTimestampFromFormattedDate(getView()?.getDate()!!)
                if (formattedDate != null) {
                    debt.datetime = formattedDate.toString()
                }
            } else {
                val formattedDateTime = DateFormatter().getTimestampFromFormattedDateTime(
                        "${getView()?.getDate()!!} ${getView()?.getTime()!!}")
                if (formattedDateTime != null) {
                    debt.datetime = formattedDateTime.toString()
                }
            }
        }

        debt.comment = getView()?.getComment()
        debt.status = "active"
        debt.tripId = currentTrip.uid

        updateDebtInDB(debt)
    }

    override fun addExpenseButtonIsPressed() {
        Log.d("Add expense button is pressed")
        getView()?.displayExpenseActivity(debt.uid, null)
    }

    override fun debtIdIsReceiverFromMainActivity(debtId: Int?) {

        Log.d("debtIdIsReceiverFromMainActivity = $debtId")

        if (debtId != null && debtId != -1) {
            getAndDisplayDebtById(debtId.toString())
        } else {
            getAndDisplayDebtDraft()
        }
    }

    override fun deleteButtonIsPressed() {

        Log.d("Delete button is pressed")
        Completable.fromAction {
            MainApplication.db.debtDAO().deleteDebt(debt.uid.toString(), "deleted")
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Debt is deleted")
                        MainApplication.bus.send(Events.DebtIsDeleted(debt))
                        getView()?.finishSelf()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error deleting debt, $e")
                        displayDatabaseError()
                    }
                })
    }

    override fun clearButtonIsPressed() {

        Log.d("Clear debt button is pressed")

        debt.datetime = System.currentTimeMillis().toString()
        debt.spentAmount = ""
        debt.comment = ""
        debt.senderId = ""

        Completable.fromAction {
            MainApplication.db.debtDAO().updateDebt(debt)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Debt is updated with empty values, deleting related expenses")
                        Completable.fromAction {
                            MainApplication.db.expenseDAO().deleteExpensesForDebt(debt.uid.toString())
                        }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : CompletableObserver {

                                    override fun onSubscribe(d: Disposable) {}

                                    override fun onError(e: Throwable) {
                                        Log.d("Error deleting expenses from DB, $e")
                                    }

                                    override fun onComplete() {
                                        Log.d("Expenses for debt are deleted")
                                        getAndDisplayDebtDraft()
                                    }
                                })
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating debt draft in DB, $e")
                    }
                })
    }

    override fun saveDebtDraft() {
        if (!::debt.isInitialized || debt.status != "draft") return

        Log.d("handle back button pressed - save debt draft")

        debt.senderId = positionToContactIdMap[getView()?.getSender()]?.uid.toString()
        debt.spentAmount= getView()?.getAmount()

        if (getView()?.getDate() != null) {

            if (getView()?.getTime() == null) {
                val formattedDate = DateFormatter().getTimestampFromFormattedDate(getView()?.getDate()!!)
                if (formattedDate != null) {
                    debt.datetime = formattedDate.toString()
                }
            } else {
                val formattedDateTime = DateFormatter().getTimestampFromFormattedDateTime(
                        "${getView()?.getDate()!!} ${getView()?.getTime()!!}")
                if (formattedDateTime != null) {
                    debt.datetime = formattedDateTime.toString()
                }
            }
        }

        debt.comment = getView()?.getComment()

        updateDebtInDB(debt)
    }

    private fun getAndDisplayDebtById(debtId: String) {

        Log.d("Receiving debt data from DB")
        MainApplication.db.debtDAO().getDebtByIds(debtId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Debt>() {
                    override fun onSuccess(debt: Debt) {
                        Log.d("Debt is received from DB, = $debt")
                        this@DebtActivityPresenter.debt = debt
                        getView()?.showDeleteButton()
                        displayDebtData()
                        getAndDisplayExpensesForDebt(debt.uid.toString())
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error receiving debt data from DB")
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("No debt for specified id exist")
                    }
                })
    }


    private fun getAllContactsFromDB() {

        Log.d("Getting contacts from DB...")
        MainApplication.db.contactDAO().getContactsForCurrentTrip()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<com.macgavrina.co_accounting.room.Contact>>() {
                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
                        Log.d("Contacts are received from DB, size = ${contactsList.size}")
                        displayContactsList(contactsList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting contacts from DB, $e")
                    }

                    override fun onComplete() {
                        Log.d("There is no contacts in DB")
                    }
                })
    }

    private fun getAndDisplayDebtDraft() {

        Log.d("Getting debt draft from DB")
        MainApplication.db.debtDAO().getDebtDraft("draft")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Debt>() {
                    override fun onSuccess(debt: Debt) {
                        Log.d("Debt draft is received from DB, = $debt")
                        this@DebtActivityPresenter.debt = debt
                        displayDebtData()

                        getView()?.showClearButton()

                        getAndDisplayExpensesForDebt(debt.uid.toString())
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        addAndDisplayDebtDraft()
                    }
                })
    }

    private fun getAndDisplayExpensesForDebt(debtId: String) {

        Log.d("Getting expenses for debt...")

        MainApplication.db.expenseDAO().getExpensesForDebt(debtId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Expense>>() {
                    override fun onSuccess(expenseList: List<Expense>) {
                        Log.d("Expenses are received from DB, size = ${expenseList.size}")
                        getView()?.hideProgress()
                        getView()?.initializeExpensesList(expenseList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting expenses from DB, $e")
                    }

                    override fun onComplete() {
                        getView()?.hideProgress()
                    }
                })
    }

    private fun displayDebtData() {
        if (senderId == null) {
            if (!debt.senderId.isNullOrEmpty() && ::friendsList.isInitialized) {
                Log.d("setSender")

                if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[debt.senderId!!.toInt()]!=null) {
                    getView()?.setSender(contactIdToPositionMap[debt.senderId!!.toInt()]!!)
                }
            }

            if (debt.senderId.isNullOrEmpty() && ::friendsList.isInitialized) {
                getView()?.setSender(0)
            }
        }

        if (debt.spentAmount != null) {
            getView()?.setAmount(debt.spentAmount!!)
        }

        if (debt.datetime != null) {
                getView()?.setDate(DateFormatter().formatDateFromTimestamp(debt.datetime!!.toLong()))
                getView()?.setTime(DateFormatter().formatTimeFromTimestamp(debt.datetime!!.toLong()))
            }

        if (debt.comment != null) {
            getView()?.setComment(debt.comment!!)
        }
    }

    private fun displayContactsList(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {

        if (contactsList.isEmpty()) {
            getView()?.showAlertAndGoToContacts("Please add at least one contact first")
        }

        friendsList = arrayOfNulls<String>(contactsList.size)
        contactsIdToNameMap = mutableMapOf()
        positionToContactIdMap = mutableMapOf()
        contactIdToPositionMap = mutableMapOf()
        var i = 0

        contactsList.forEach { contact ->
            friendsList[i] = contact.alias.toString()

            contactsIdToNameMap[contact.uid.toString()] = contact
            positionToContactIdMap[i] = contact
            contactIdToPositionMap[contact.uid] = i
            i = i + 1
        }


        getView()?.setupSenderSpinner(friendsList)

        if (senderId == null) {

            if (::debt.isInitialized &&  debt.senderId != null && debt.senderId!!.isNotEmpty() && debt.senderId != "null") {

                if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[debt.senderId?.toInt()] != null) {
                    getView()?.setSender(contactIdToPositionMap[debt.senderId?.toInt()]!!)
                }
            }
        }
    }

    private fun displayDatabaseError() {
        getView()?.displayToast("Database error")
        getView()?.hideProgress()
    }

    private fun updateDebtInDB(debt: Debt) {

        Log.d("Updating debt in DB...")

        Completable.fromAction {
            MainApplication.db.debtDAO().updateDebt(debt)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Debt is updated")
                        getView()?.finishSelf()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating debt, $e")
                        displayDatabaseError()
                    }
                })
    }

    private fun addAndDisplayDebtDraft() {

        Completable.fromAction {
            val debt = Debt()
            debt.status = "draft"
            debt.datetime = System.currentTimeMillis().toString()
            MainApplication.db.debtDAO().insertDebt(debt)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Debt is added")
                        getAndDisplayDebtDraft()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error adding debt draft, $e")
                        displayDatabaseError()
                    }
                })
    }
}