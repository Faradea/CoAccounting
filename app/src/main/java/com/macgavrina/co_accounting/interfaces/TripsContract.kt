package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Trip

interface TripsContract {

    interface View:BaseViewContract {

        fun displayRevertChangesAction()

        fun initializeList(tripsList: List<Trip>)

        fun updateList()

        fun showProgress()

        fun hideProgress()

        fun displayToast(text:String)

        fun displayOnDeleteTripSnackBar()
    }

    interface Presenter:BasePresenterContract<View> {

        fun addTripButtonIsPressed()

        fun tripItemIsSelected(selectedTripId:Int)

        fun undoDeleteTripButtonIsPressed()

    }
}