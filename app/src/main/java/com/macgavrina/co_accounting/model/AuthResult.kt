package com.macgavrina.co_accounting.model

//just for auth response parsing

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AuthResult(
        @Expose
        @SerializedName("userToken") val userToken: String
)