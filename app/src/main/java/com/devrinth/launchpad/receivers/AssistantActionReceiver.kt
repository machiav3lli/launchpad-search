package com.devrinth.launchpad.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AssistantActionReceiver( var callback: () -> Unit) : BroadcastReceiver() {

    companion object {
        const val ACTION_OVERLAY_SHOW = "com.devrinth.launchpad.ACTION_SHOW"
        const val ACTION_OVERLAY_HIDE = "com.devrinth.launchpad.ACTION_HIDE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        callback()
    }
}