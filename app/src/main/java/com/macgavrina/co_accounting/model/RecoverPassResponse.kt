package com.macgavrina.co_accounting.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RecoverPassResponse(

        @Expose
        @SerializedName("result") val result: String
)