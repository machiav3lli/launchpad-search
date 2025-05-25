package com.devrinth.launchpad.search.plugins
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SearchSuggestionsPlugin(mContext: Context) : SearchPlugin(mContext) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
    private lateinit var searchEngineQ : String

    private var isProcessing = false

    private val client = OkHttpClient()

    private val GOOGLE_API = "https://suggestqueries.google.com/complete/search?client=firefox&q=%s"

    override fun pluginInit() {
        super.pluginInit()
        val search = sharedPreferences.getString("setting_search_plugin_engine", mContext.resources.getString(R.string.search_google_query) ).toString().split("|")[0]
        searchEngineQ = if (search != "custom") {
            sharedPreferences.getString("setting_search_plugin_engine", mContext.resources.getString(R.string.search_google_query) ).toString().split("|")[1]
        } else {
            sharedPreferences.getString("setting_search_plugin_custom_engine", mContext.resources.getString(R.string.search_google_query).split("|")[1] ).toString()
        }

    }

    override fun pluginProcess(query: String) {
        super.pluginProcess(query)
        isProcessing = false
        CoroutineScope(Dispatchers.Main).launch {
            pluginResult( processSuggestions(query), query )
            isProcessing = false
        }

    }

    private suspend fun processSuggestions(query: String) : List<ResultAdapter> {
        return withContext(Dispatchers.Default) {
            try {
                val searchSuggestions = ArrayList<ResultAdapter>()

                val request = Request.Builder()
                    .url(GOOGLE_API.format(query.lowercase()))
                    .build()

                val response = client.newCall(request).execute()

                val mainObj = JSONArray( response.body?.string() )
                val suggestionArray = mainObj.getJSONArray(1)

                for (i in 0 until suggestionArray.length() ) {
                    if (i > 4)
                        break
                    val suggestion = suggestionArray.getString(i)
                    searchSuggestions.add(
                        ResultAdapter(
                            suggestion,
                            null,
                            null,
                            IntentUtils.getLinkIntent( searchEngineQ.format( URLEncoder.encode(suggestion, StandardCharsets.UTF_8.toString()) ) ),
                            null
                        )
                    )

                }
                searchSuggestions

            } catch (e : Exception) {
                Log.e("jaxy", e.toString())
                emptyList()
            }
        }
    }

}