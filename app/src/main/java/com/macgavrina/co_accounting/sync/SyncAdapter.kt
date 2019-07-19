//package com.macgavrina.co_accounting.sync
//
//import android.accounts.Account
//import android.content.*
//import android.os.Bundle
//import com.macgavrina.co_accounting.MainApplication
//import com.macgavrina.co_accounting.logging.Log
//
//
///**
// * Handle the transfer of data between a server and an
// * app, using the Android sync adapter framework.
// */
//
//class SyncAdapter(applicationContext: Context, b: Boolean) : AbstractThreadedSyncAdapter(MainApplication.applicationContext(), true) {
//
//    val mContentResolver: ContentResolver = MainApplication.applicationContext().contentResolver
//
//    override fun onPerformSync(p0: Account?, p1: Bundle?, p2: String?, p3: ContentProviderClient?, p4: SyncResult?) {
//        Log.d("perform sync")
//    }
//}
//
//
///*public class SyncAdapter extends AbstractThreadedSyncAdapter {
//    ...
//    // Global variables
//    // Define a variable to contain a content resolver instance
//    ContentResolver mContentResolver;
//*
//     * Set up the sync adapter
//
//
//    public SyncAdapter(Context context, boolean autoInitialize) {
//        super(context, autoInitialize);
//         * If your app uses a content resolver, get an instance of it
//         * from the incoming Context
//
//
//        mContentResolver = context.getContentResolver();
//    }
//    ...
//*
//     * Set up the sync adapter. This form of the
//     * constructor maintains compatibility with Android 3.0
//     * and later platform versions
//
//
//    public SyncAdapter(
//            Context context,
//    boolean autoInitialize,
//    boolean allowParallelSyncs) {
//        super(context, autoInitialize, allowParallelSyncs);
//         * If your app uses a content resolver, get an instance of it
//         * from the incoming Context
//
//
//        mContentResolver = context.getContentResolver();
//        ...
//    }
//}*/
