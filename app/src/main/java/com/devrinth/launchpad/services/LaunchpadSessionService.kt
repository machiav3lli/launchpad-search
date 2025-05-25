package com.devrinth.launchpad.services


import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.view.View
import android.widget.Toast

import androidx.preference.PreferenceManager
import com.devrinth.launchpad.R
import com.devrinth.launchpad.modals.OverlayState

import com.devrinth.launchpad.receivers.AssistantActionReceiver
import com.devrinth.launchpad.search.SearchWindow

class LaunchpadSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return MyVoiceInteractionSession(this)
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private class MyVoiceInteractionSession(context: Context?) :
        VoiceInteractionSession(context) {

        private var hideReceiver : AssistantActionReceiver = AssistantActionReceiver {
            this.finish()
        }
        private val sharedPreferences: SharedPreferences
        private var lastUiMode: Int

        private var mSearchWindow: SearchWindow

        init {

            mSearchWindow = SearchWindow(context!!)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            lastUiMode = context.resources.configuration.uiMode

            if (Build.VERSION.SDK_INT >= 33) {
                context.registerReceiver(hideReceiver, IntentFilter(AssistantActionReceiver.ACTION_OVERLAY_HIDE), RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(hideReceiver, IntentFilter(AssistantActionReceiver.ACTION_OVERLAY_HIDE))
            }

            mSearchWindow.onWindowClose { this.finish() }

        }

        override fun finish() {
            super.finish()
            mSearchWindow.unload()
        }

        override fun hide() {
            if (lastUiMode != context.resources.configuration.uiMode) {
                this.finish()
                return
            }
            lastUiMode = context.resources.configuration.uiMode
            mSearchWindow.hideWindow()
        }

        override fun onCreateContentView(): View {
            super.onCreateContentView()

            mSearchWindow.createLaunchpadWindow(window.window!!)
            return mSearchWindow.getLaunchpadWindow()
        }

        override fun onShow(args: Bundle?, flags: Int) {
            super.onShow(args, flags)
            if (OverlayState.isOverlayActive) {
                Toast.makeText(context,
                    context.getString(R.string.general_warning_assistant_service), Toast.LENGTH_SHORT).show()
                this.finish()
            }
            mSearchWindow.showWindow()
            mSearchWindow.showKeyboard()
        }

    }
}