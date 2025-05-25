package com.devrinth.launchpad.adapters

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.service.voice.VoiceInteractionSessionService

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.devrinth.launchpad.R
import com.devrinth.launchpad.receivers.AssistantActionReceiver


class SearchSuggestionListAdapter(private val mSuggestions: List<ResultAdapter>, private var mContext: Context) : RecyclerView.Adapter<SearchSuggestionListAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(mContext)

    private var closeOnClick =
        sharedPreferences.getBoolean("setting_close_on_action", true)

    private var reloadReceiver : AssistantActionReceiver = AssistantActionReceiver {
        closeOnClick =
            sharedPreferences.getBoolean("setting_close_on_action", true)
    }
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(reloadReceiver, IntentFilter(AssistantActionReceiver.ACTION_OVERLAY_SHOW),
                VoiceInteractionSessionService.RECEIVER_EXPORTED
            )
        } else {
            mContext.registerReceiver(reloadReceiver, IntentFilter(AssistantActionReceiver.ACTION_OVERLAY_SHOW))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.search_suggestion_view, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mSuggestions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mResultAdapter : ResultAdapter = mSuggestions[position]

        holder.searchTerm.text = mResultAdapter.value
        holder.parentView.setOnClickListener {
            if (closeOnClick) {
                mContext.sendBroadcast(Intent(AssistantActionReceiver.ACTION_OVERLAY_HIDE))
            }
            mContext.startActivity( mResultAdapter.action1 )
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val searchTerm: TextView = itemView.findViewById(R.id.search_suggestion_text)

        val parentView: View = itemView
    }
}