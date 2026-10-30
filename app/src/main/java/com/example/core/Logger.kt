package com.example.core

import android.util.Log

object Logger {
    private const val TAG = "Lumora"

    fun d(message: String) {
        Log.d(TAG, sanitize(message))
    }

    fun i(message: String) {
        Log.i(TAG, sanitize(message))
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(TAG, sanitize(message), throwable)
        } else {
            Log.w(TAG, sanitize(message))
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, sanitize(message), throwable)
        } else {
            Log.e(TAG, sanitize(message))
        }
    }

    private fun sanitize(input: String): String {
        // Redact email addresses and token-like strings to avoid accidental PII logging
        return input.replace(Regex("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+"), "[REDACTED_EMAIL]")
    }
}
