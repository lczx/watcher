package net.hax.niatool

import android.app.Application
import org.slf4j.impl.HandroidLoggerAdapter


class WatcherApplication : Application() {

    companion object {
        init {
            HandroidLoggerAdapter.APP_NAME = "Watcher"
            HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
        }
    }

}
