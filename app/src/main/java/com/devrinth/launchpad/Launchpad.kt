package com.devrinth.launchpad

import android.app.Application
import com.google.android.material.color.DynamicColors

class Launchpad : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

}