package com.turingheights.wally

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import timber.log.Timber

@HiltAndroidApp
class WallyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ProdLoggerTree)
        }
    }

    object ProdLoggerTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            if (priority == Log.INFO || priority == Log.WARN) {
                val breadcrumb = Breadcrumb().apply {
                    this.category = tag ?: "Wally"
                    this.message = message
                    this.level = if (priority == Log.WARN) SentryLevel.WARNING else SentryLevel.INFO
                }
                Sentry.addBreadcrumb(breadcrumb)
                return
            }

            if (t != null) {
                Sentry.addBreadcrumb("${tag ?: "Wally"}: $message")
                Sentry.captureException(t)
            } else {
                Sentry.captureMessage("${tag ?: "Wally"}: $message")
            }
        }
    }
}
