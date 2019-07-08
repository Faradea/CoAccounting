package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

const val CONTACTS_NAV_MENU_ITEM_POSITION = 0
const val DEBTS_NAV_MENU_ITEM_POSITION = 1
const val TRIPS_NAV_MENU_ITEM_POSITION = 2
const val CALCULATIONS_MENU_ITEM_POSITION = 3

const val CONTACTS_FRAGMENT_ID = 0
const val DEBTS_FRAGMENT_ID = 1
const val TRIPS_FRAGMENT_ID = 2
const val CALCULATIONS_FRAGMENT_ID = 3

class MainViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    private var checkedNavMenuItemPosition: MutableLiveData<Int> = MutableLiveData(-1)
    private var displayedFragment: MutableLiveData<Int> = MutableLiveData(-1)

    private val _goToTrip = SingleLiveEvent<Int>()
    val goToTrip: LiveData<Int>
        get() = _goToTrip

    private val _goToContact = SingleLiveEvent<Int> ()
    val goToContact : LiveData<Int>
        get() = _goToContact

    private val _goToDebt = SingleLiveEvent<Int> ()
    val goToDebt: LiveData<Int>
        get() = _goToDebt

    private val _goToDebtAsInitialScreen = SingleLiveEvent<Int> ()
    val goToDebtAsInitialScreen: LiveData<Int>
        get() = _goToDebtAsInitialScreen

    //internal val toastMessage = SingleLiveEvent<String>()
    //internal val snackbarMessage = SingleLiveEvent<String>()

    init {
        compositeDisposable.add(
                TripRepository().getAllRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ tripsList ->
                    if (!tripsList.isNullOrEmpty()) {
                        Log.d("Go to debts as initial screen")
                        _goToDebtAsInitialScreen.value = 0
                    } else {
                        Log.d("Trip view is empty")
                    }
                }, {error ->
                    Log.d("Error getting trips from DB, $error")
                })
        )

        subscribeToEventBus()
    }

    private fun subscribeToEventBus() {
        compositeDisposable.add(
                MainApplication
                .bus
                .toObservable()
                .subscribe { `object` ->
                    when (`object`) {
//                        is Events.FromRegisterToLoginEvent -> {
//                            Log.d("Catch Events.FromRegisterToLoginEvent event")
//                            //getView()?.displayLoginFragment(`object`.myEnteredLogin)
//                        }
//                        is Events.RegisterIsSuccessful -> {
//                            Log.d("Catch Events.RegisterIsSuccessful event")
//                            UserProvider().loadUser(this)
//                            //getView()?.displayRegisterSuccessDialog(`object`.myTitle, `object`.myText)
//                        }
//                        is Events.RecoverPassIsSuccessful -> {
//                            Log.d("Catch Events.RecoverPassIsSuccessful event")
//                            //getView()?.displayRecoverPassSuccessDialog(`object`.myTitle, `object`.myText, `object`.myEnteredLogin)
//                        }
//                        is Events.LogoutFinished -> {
//                            Log.d("Catch Events.LogoutFinished event")
//                            UserProvider().loadUser(this)
//                            //getView()?.displayLoginFragment(null)
//                        }
//                        is Events.LoginIsSuccessful -> {
//                            Log.d("Catch Events.LoginIsSuccessful event")
//                            UserProvider().loadUser(this)
//                            //getView()?.displayMainFragment()
//                        }
//                        is Events.FromLoginToRegister -> {
//                            Log.d("Catch Events.FromLoginToRegister event")
//                            //getView()?.displayRegisterFragment(`object`.myEnteredLogin)
//                        }
//                        is Events.FromLoginToRecoverPass -> {
//                            Log.d("Catch Events.FromLoginToRecoverPass event")
//                            //getView()?.displayRecoverPassFragment(`object`.myEnteredLogin)
//                        }
                        is Events.AddContact -> {
                            Log.d("Catch Events.AddContact event")
                            _goToContact.value = -1
                        }
                        is Events.ContactIsAdded -> {
                            Log.d("Catch Events.ContactIsAdded event")
                            //displayedFragment.value = CONTACTS_FRAGMENT_ID
                        }
                        is Events.OnClickContactList -> {
                            Log.d("Catch Events.OnClickContactList event")
                            _goToContact.value = `object`.myUid?.toIntOrNull() ?: -1
                        }
//                            is Events.ContactEditingIsFinished -> {
//                                Log.d("Catch Events.ContactEditingIsFinished event")
//                                getView()?.displayContactsFragment()
//                            }
//                        is Events.ContactIsDeleted -> {
//                            Log.d("Catch Events.ContactIsDeleted event")
//                            lastDeletedContact = `object`.contact
//                            getView()?.displayOnDeleteContactSnackBar()
//                        }
                        is Events.AddTrip -> {
                            Log.d("catch Events.AddTrip event")
                            _goToTrip.value = -1
                        }
                        is Events.AddDebt -> {
                            Log.d("Catch Events.AddDebt event")
                            _goToDebt.value = -1
                        }
                        is Events.DebtIsAdded -> {
                            Log.d("Catch Events.DebtIsAdded event")
                            //getView()?.displayDebtsFragment(false)
                        }

                        is Events.OnClickDebtItemList -> {
                            Log.d("Catch Events.OnClickDebtItemList event")
                            _goToDebt.value = `object`.myUid?.toIntOrNull() ?: -1
                        }
                        is Events.OnClickTripList -> {
                            Log.d("Catch Events.OnClickTripList event")
                            _goToTrip.value = `object`.tripId?.toIntOrNull() ?: -1
                        }
                        is Events.DefaultTripIsCreated -> {
                            Log.d("Catch Events.DefaultTripIsCreated event")
                            _goToDebtAsInitialScreen.value = 0
                        }
                    }
                }
        )
    }

    fun viewIsDestroyed() {
        compositeDisposable.clear()
    }

    fun getCheckedNavMenuItemPosition(): LiveData<Int> {
        return checkedNavMenuItemPosition
    }

    fun navMenuItemIsChecked(itemPosition: Int) {
        when (itemPosition) {
            CONTACTS_NAV_MENU_ITEM_POSITION -> displayedFragment.value = CONTACTS_FRAGMENT_ID
            DEBTS_NAV_MENU_ITEM_POSITION -> displayedFragment.value = DEBTS_FRAGMENT_ID
            TRIPS_NAV_MENU_ITEM_POSITION -> displayedFragment.value = TRIPS_FRAGMENT_ID
            CALCULATIONS_MENU_ITEM_POSITION -> displayedFragment.value = CALCULATIONS_FRAGMENT_ID
        }
    }

    fun goToContactsFromDebtRequest() {
        checkedNavMenuItemPosition.value = CONTACTS_NAV_MENU_ITEM_POSITION
        displayedFragment.value = CONTACTS_FRAGMENT_ID
    }

    fun goToCurrentTripFromDebtRequest() {
        compositeDisposable.add(
                TripRepository().getCurrentTrip()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({trip ->
                    _goToTrip.value = trip.uid
                }, {error ->
                    Log.d("Error getting current trip from DB, $error")
                })
        )
    }

    fun getDisplayedFragmentId(): LiveData<Int> {
        return displayedFragment
    }
}