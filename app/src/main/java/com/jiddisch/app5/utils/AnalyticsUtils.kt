package com.jiddisch.app5.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsUtils {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun init() {
        firebaseAnalytics = Firebase.analytics
    }

    fun logEvent(eventName: String, bundle: Bundle?) {
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    fun logEvent(eventName: String, vararg params: Pair<String, String>) {
        val bundle = Bundle()
        for ((key, value) in params) {
            bundle.putString(key, value)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
