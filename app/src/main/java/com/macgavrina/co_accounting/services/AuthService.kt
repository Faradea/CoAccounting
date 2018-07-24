package com.macgavrina.co_accounting.services

import com.google.gson.GsonBuilder
import com.macgavrina.co_accounting.model.AuthResult
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

const val server:String = "http://in-debt.ru/vi_api/"

interface AuthService {
    @FormUrlEncoded
    @POST("auth")
    fun performPostCallWithQuery(@Field("email") email:String, @Field("pass") pass:String): Call<AuthResult>

    companion object ApiFactory{
        fun create():AuthService{

            val gson = GsonBuilder().create()

            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(server)
                    .build()
            return retrofit.create(AuthService::class.java)
        }

    }
}