package com.io.lkconsultants

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.io.lkconsultants.reverb.ChatNotifier
import com.room.roomy.retrofit.TokenProvider
import java.util.concurrent.atomic.AtomicInteger

class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenProvider.init(applicationContext)
        ChatNotifier.ensureChannel(applicationContext)
        registerActivityLifecycleCallbacks(AppForegroundTracker)
    }

    object AppForegroundTracker : ActivityLifecycleCallbacks {
        private val started = AtomicInteger(0)
        val isForeground: Boolean get() = started.get() > 0

        override fun onActivityStarted(activity: Activity) { started.incrementAndGet() }
        override fun onActivityStopped(activity: Activity) { started.decrementAndGet() }

        override fun onActivityCreated(activity: Activity, b: Bundle?) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, b: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}
