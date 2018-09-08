package com.macgavrina.co_accounting.services

import com.google.gson.GsonBuilder
import com.macgavrina.co_accounting.model.DefaultServiceResponse
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface ContactsService {
    @FormUrlEncoded
    @POST("contacts")
    fun addContact(@Header("Authorization") userToken:String, @Field("email") email:String, @Field("alias") alias:String): Single<DefaultServiceResponse>

    companion object ApiFactory{
        fun create():ContactsService{

            val gson = GsonBuilder().create()

            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(SERVER_URL)
                    .build()
            return retrofit.create(ContactsService::class.java)
        }

    }
}