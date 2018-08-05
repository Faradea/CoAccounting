package com.macgavrina.co_accounting.logging

import android.text.TextUtils

object Log {

    private var mLoggingEnabled = false
    private val LOG_TAG = "InDebtApp"

    private val logPrefix: String
        get() {
            val className = Log::class.java.name
            val traces = Thread.currentThread().stackTrace
            var found = false

            for (trace in traces) {
                try {
                    if (found) {
                        if (!trace.className.startsWith(className)) {
                            val clazz = Class.forName(trace.className)
                            return "[" + getClassName(clazz) + ":" + trace.methodName + ":" + trace.lineNumber + "]: "
                        }
                    } else if (trace.className.startsWith(className)) {
                        found = true
                    }
                } catch (ignored: ClassNotFoundException) {
                }

            }

            return "[]: "
        }

    fun setDebugLogging(enabled: Boolean) {
        mLoggingEnabled = enabled
    }

    fun d(msg: String) {
        if (mLoggingEnabled) {
            android.util.Log.d(LOG_TAG, logPrefix + msg)
        }
    }

    private fun getClassName(clazz: Class<*>?): String {
        return if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.simpleName)) {
                clazz.simpleName
            } else getClassName(clazz.enclosingClass)

        } else ""

    }

}
