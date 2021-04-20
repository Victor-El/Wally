package me.codeenzyme.wally

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.sentry.Sentry
import timber.log.Timber

@HiltAndroidApp
class WallyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ProdLoggerTree)
        }
    }

    object ProdLoggerTree: Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            if (t != null) {
                Sentry.captureException(t)
            }

            Sentry.captureMessage("$tag : $message")
        }

    }
}