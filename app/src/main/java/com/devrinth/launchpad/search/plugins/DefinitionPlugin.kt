package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.content.res.AssetManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader

import java.io.InputStreamReader


class DefinitionPlugin(mContext: Context) : SearchPlugin(mContext) {

    private var mainObject: JSONObject? = null
    private var isProcessing = false

    override fun pluginInit() {
        if (mainObject != null) {
            INIT = true
            return
        }

        try {
            readAndParseJsonAsync(mContext, "filtered.json") {
                mainObject = it
                INIT = true
            }
        }
        catch (_: Exception) {}
        finally {}
    }

    override fun pluginProcess(query: String) {
        if (!INIT || query.isEmpty() || query.length < 3 || isProcessing) {
            pluginResult(emptyList(), "")
            return
        }
        isProcessing = true

        CoroutineScope(Dispatchers.Main).launch {
            pluginResult(filterDefinition(query), query)
            isProcessing = false
        }

    }

    private suspend fun filterDefinition(query: String): List<ResultAdapter> {
        return withContext(Dispatchers.Default) {
            val filteredDefinition = arrayListOf<ResultAdapter>()

            try {

                val wordObject = mainObject?.getJSONObject(query.trim().uppercase())
                val meaningArray = wordObject?.getJSONArray("MEANINGS")

                filteredDefinition.add(
                    ResultAdapter(
                        mContext.getString(R.string.plugin_definition_result_title).format(query.lowercase()),
                        mContext.getString(R.string.plugin_definition_result).trimIndent().format(
                            wordObject!!.getJSONArray("SYNONYMS").join(", ").replace("\"", ""),
                            wordObject.getJSONArray("ANTONYMS").join(", ").replace("\"", "")
                        ),
                        AppCompatResources.getDrawable(mContext, R.drawable.baseline_menu_book_24),
                        null,
                        null
                    )
                )

                var loopCount = 0
                for (i in 0 until meaningArray!!.length()) {
                    if (loopCount > 1)
                        break
                    val element = meaningArray.getJSONArray(i)
                    if (element is JSONArray) {
                        filteredDefinition.add(
                            ResultAdapter(
                                "(${element.getString(0).lowercase()}) ${query.lowercase()}",
                                element.getString(1),
                                AppCompatResources.getDrawable(
                                    mContext,
                                    R.drawable.baseline_menu_book_24
                                ),
                                null,
                                null
                            )
                        )
                        loopCount++
                    }
                }

            } catch (_: Exception) {
            }
            filteredDefinition
        }
    }
    private fun readAndParseJsonAsync(context: Context, fileName: String, onResult: (JSONObject?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val content = readAssetFile(context.assets, fileName)
                val jsonObject = JSONObject(content)
                withContext(Dispatchers.Main) {
                    onResult(jsonObject)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    private suspend fun readAssetFile(assetManager: AssetManager, fileName: String): String {
        val stringBuilder = StringBuilder()
        assetManager.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                    yield()
                }
            }
        }
        return stringBuilder.toString()
    }
}