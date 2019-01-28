package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.DebtActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.ReceiverWithAmount
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
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
                                Log.d("AddDebtReceiverWithAmountListIsChanged, newAmount = $newAmount, position = $positionInList")
                                receiverWithAmountList[positionInList].amount = newAmount.toFloat()
                            }
                            is Events.OnClickExpenseItemList -> {
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
        Log.d("DebtActivity view id ready")
        //getView()?.getEmail()?.length!! > 0

        getView()?.hideProgress()

        Log.d("getting all contacts from db...")
        getAllContactsFromDB()

        if (::debt.isInitialized) {
            getAndDisplayExpensesForDebt(debt.uid.toString())
        }

        if (::friendsList.isInitialized && senderId != null) {
            Log.d("view is ready, set senderId = $senderId")

            if (::contactIdToPositionMap.isInitialized && contactIdToPositionMap[senderId!!]!=null) {
                getView()?.setSender(contactIdToPositionMap[senderId!!]!!)
            }
        }
    }

    override fun viewIsPaused() {

        Log.d("viewIsPaused, saving senderId...")
        Log.d("getView()?.getSender() = ${getView()?.getSender()}")
        Log.d("positionToContactIdMap = $positionToContactIdMap")
        Log.d("positionToContactIdMap[getView()?.getSender()]?.uid = ${positionToContactIdMap[getView()?.getSender()]?.uid}")

        senderId = positionToContactIdMap[getView()?.getSender()]?.uid
        //Log.d("view is paused, save senderId = $senderId")
    }

    override fun viewIsCreated() {
        super.viewIsCreated()

        getView()?.hideDeleteButton()
        getView()?.hideClearButton()
    }

    private fun getAndDisplayExpensesForDebt(debtId: String) {

        MainApplication.db.expenseDAO().getExpensesForDebt(debtId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Expense>>() {
                    override fun onSuccess(expenseList: List<Expense>) {
                        getView()?.hideProgress()

                        expenseList.forEach { expense ->
                            Log.d("expense: debtId = ${expense.debtId}, amount = ${expense.totalAmount}, receiversList = ${expense.receiversList}")
                        }

                        getView()?.initializeExpensesList(expenseList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                        getView()?.hideProgress()
                    }
                })
    }

    private fun displayContactsList(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {

        Log.d("contacts list is loaded")

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

        Log.d("$contactsList")
        Log.d("contactsIdToNameMap = $contactsIdToNameMap")
        Log.d("positionToContactIdMap = $positionToContactIdMap")
        Log.d("contactIdToPositionMap = $contactIdToPositionMap")


        getView()?.setupSenderSpinner(friendsList)

        if (senderId == null) {

            if (::debt.isInitialized &&  debt.senderId != null && debt.senderId!!.isNotEmpty() && debt.senderId != "null") {
                Log.d("setSender, ${debt.senderId}")

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


    private fun getAndDisplayDebtDraft() {
        MainApplication.db.debtDAO().getDebtDraft("draft")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Debt>() {
                    override fun onSuccess(debt: Debt) {

                        Log.d("draft debt id = ${debt.uid}")

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
                        getAndDisplayDebtDraft()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error adding debt draft, $e")
                        displayDatabaseError()
                    }
                })
    }

    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        addDebtButtonEnabled = areFilled
    }

    override fun addButtonIsPressed() {

        Log.d("handle add button pressed - update debt")
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

        updateDebtInDB(debt)
    }

    override fun addReceiverButtonIsPressed() {

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
        Completable.fromAction {
            MainApplication.db.debtDAO().deleteDebt(debt.uid.toString(), "deleted")
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
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
                        Completable.fromAction {
                            MainApplication.db.expenseDAO().deleteExpensesForDebt(debt.uid.toString())
                        }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : CompletableObserver {

                                    override fun onSubscribe(d: Disposable) {}

                                    override fun onError(e: Throwable) {
                                        Log.d(e.toString())
                                    }

                                    override fun onComplete() {
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

    private fun updateDebtInDB(debt: Debt) {

        Completable.fromAction {
            MainApplication.db.debtDAO().updateDebt(debt)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        //getView()?.hideProgress()

                        Log.d("debt is updated")
                        //MainApplication.bus.send(Events.DebtIsAdded())
                        getView()?.finishSelf()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating debt, $e")
                        displayDatabaseError()
                    }
                })
    }

    private fun getAndDisplayDebtById(debtId: String) {
        MainApplication.db.debtDAO().getDebtByIds(debtId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Debt>() {
                    override fun onSuccess(debt: Debt) {
                        Log.d("onDebtLoaded, display data...")
                        this@DebtActivityPresenter.debt = debt

                        getView()?.showDeleteButton()

                        displayDebtData()

                        getAndDisplayExpensesForDebt(debt.uid.toString())
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("No debt for specified id exist")
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

    private fun getAllContactsFromDB() {
        MainApplication.db.contactDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<com.macgavrina.co_accounting.room.Contact>>() {
                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
                        displayContactsList(contactsList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("There is no contacts in DB")
                    }
                })
    }
}