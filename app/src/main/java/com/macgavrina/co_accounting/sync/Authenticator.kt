//package com.macgavrina.co_accounting.sync
//
//import android.accounts.NetworkErrorException
//import android.accounts.Account
//import android.accounts.AccountAuthenticatorResponse
//import android.os.Bundle
//import android.accounts.AbstractAccountAuthenticator
//import android.content.Context
//
////ToDo REFACT удалить всю папку если получиться сделать синхронизацию по появлению сети без этого всего
//
//class Authenticator(context: Context) : AbstractAccountAuthenticator(context) {
//
//    // Editing properties is not supported
//    override fun editProperties(
//            r: AccountAuthenticatorResponse, s: String): Bundle {
//        throw UnsupportedOperationException()
//    }
//
//    // Don't add additional accounts
//    @Throws(NetworkErrorException::class)
//    override fun addAccount(
//            r: AccountAuthenticatorResponse,
//            s: String,
//            s2: String,
//            strings: Array<String>,
//            bundle: Bundle): Bundle? {
//        return null
//    }
//
//    // Ignore attempts to confirm credentials
//    @Throws(NetworkErrorException::class)
//    override fun confirmCredentials(
//            r: AccountAuthenticatorResponse,
//            account: Account,
//            bundle: Bundle): Bundle? {
//        return null
//    }
//
//    // Getting an authentication token is not supported
//    @Throws(NetworkErrorException::class)
//    override fun getAuthToken(
//            r: AccountAuthenticatorResponse,
//            account: Account,
//            s: String,
//            bundle: Bundle): Bundle {
//        throw UnsupportedOperationException()
//    }
//
//    // Getting a label for the auth token is not supported
//    override fun getAuthTokenLabel(s: String): String {
//        throw UnsupportedOperationException()
//    }
//
//    // Updating user credentials is not supported
//    @Throws(NetworkErrorException::class)
//    override fun updateCredentials(
//            r: AccountAuthenticatorResponse,
//            account: Account,
//            s: String, bundle: Bundle): Bundle {
//        throw UnsupportedOperationException()
//    }
//
//    // Checking features for the account is not supported
//    @Throws(NetworkErrorException::class)
//    override fun hasFeatures(
//            r: AccountAuthenticatorResponse,
//            account: Account, strings: Array<String>): Bundle {
//        throw UnsupportedOperationException()
//    }
//}