package com.macgavrina.co_accounting.customsync

import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.sync.SyncAdapter

class SyncService() {

    //ToDo сделать singleton и syncronized syncData

    companion object {

        const val SYNC_UPLOAD = "upload"
        const val SYNC_DOWNLOAD = "download"

        fun syncData(syncUpload: Boolean, syncDownload: Boolean) {

            if (syncUpload) {
                Log.d("syncUpload")
            }

            if (syncDownload) {
                Log.d("syncDownload")
            }

        }
    }
}