//package com.macgavrina.co_accounting.sync
//
//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//
///**
// * A bound Service that instantiates the authenticator
// * when started.
// */
//
//class AuthenticatorService: Service() {
//
//    lateinit var mAuthenticator:Authenticator
//
//    override fun onCreate() {
//        super.onCreate()
//        mAuthenticator = Authenticator(this)
//    }
//    override fun onBind(p0: Intent?): IBinder? {
//        return mAuthenticator.iBinder
//    }
//
//}
