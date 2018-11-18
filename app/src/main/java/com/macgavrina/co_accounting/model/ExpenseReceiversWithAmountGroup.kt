package com.macgavrina.co_accounting.model

data class ExpenseReceiversWithAmountGroup (
        val expenseId: String,
        var receiverNamesList:String,
        var totalAmount:String?
)