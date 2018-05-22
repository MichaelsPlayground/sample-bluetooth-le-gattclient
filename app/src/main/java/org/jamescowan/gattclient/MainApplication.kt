package org.jamescowan.gattclient

import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import timber.log.Timber

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("Application started")
    }


}