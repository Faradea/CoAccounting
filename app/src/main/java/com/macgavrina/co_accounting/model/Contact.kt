package com.macgavrina.co_accounting.model

//Пока класс не используется - достаточно room.Contact
data class Contact (
        val uid:Int?,
        val email:String,
        val alias:String?,
        val friendId: String?
)