package com.macgavrina.co_accounting.services

import com.google.gson.GsonBuilder
import com.macgavrina.co_accounting.model.AuthResponse
import com.macgavrina.co_accounting.model.RecoverPassResponse
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.*

const val SERVER_URL:String = "http://in-debt.ru/vi_api/"

interface AuthService {
    @FormUrlEncoded
    @POST("auth")
    fun authCall(@Field("email") email:String, @Field("pass") pass:String): Single<AuthResponse>

    @FormUrlEncoded
    @POST("recover")
    fun recoverPassCall(@Field("email") email:String): Single<RecoverPassResponse>

    companion object ApiFactory{
        fun create():AuthService{

            val gson = GsonBuilder().create()

            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(SERVER_URL)
                    .build()
            return retrofit.create(AuthService::class.java)
        }

    }
}