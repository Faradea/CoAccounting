package com.macgavrina.co_accounting.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.macgavrina.co_accounting.MainApplication

class SyncService : Service() {
    /*
     * Instantiate the sync adapter object.
     */
    override fun onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized(sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = SyncAdapter(MainApplication.applicationContext(), true)
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    override fun onBind(intent: Intent): IBinder {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSyncAdapter!!.syncAdapterBinder
    }

    companion object {

        // Constants
        // The authority for the sync adapter's content provider
        const val AUTHORITY = "com.macgavrina.co_accounting.sync.provider"
        // An account type, in the form of a domain name
        const val ACCOUNT_TYPE = "coaccounting.com"
        // The account name
        const val ACCOUNT = "dummyaccount"

        const val SYNC_UPLOAD = "upload"
        const val SYNC_DOWNLOAD = "download"

        // Storage for an instance of the sync adapter
        private var sSyncAdapter: SyncAdapter? = null
        // Object to use as a thread-safe lock
        private val sSyncAdapterLock = Any()
    }
}