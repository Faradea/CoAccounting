package com.macgavrina.co_accounting.model

data class RecieverWithAmount (
        val receiverName:String,
        var amount:Float,
        val positionInList: Int
)