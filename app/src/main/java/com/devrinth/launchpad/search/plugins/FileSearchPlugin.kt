package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Build
import android.util.Log
import android.os.Environment
import android.util.Size
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.db.FileDatabase
import com.devrinth.launchpad.db.FileEntity
import com.devrinth.launchpad.search.SearchPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import androidx.core.graphics.drawable.toDrawable

class FileSearchPlugin(mContext: Context) : SearchPlugin(mContext) {

    private val db = FileDatabase.getInstance(mContext)
    private val dao = db.cachedFiles()
    private var isProcessing = false
    private val thumbnailCache = ConcurrentHashMap<String, BitmapDrawable?>()

    override fun pluginInit() {
        CoroutineScope(Dispatchers.IO).launch {
            refreshFileCache()
        }
        super.pluginInit()
    }

    override fun pluginProcess(query: String) {
        if (!INIT || query.isEmpty() || query.length < 2 || isProcessing) {
            pluginResult(emptyList(), "")
            return
        }
        isProcessing = true

        CoroutineScope(Dispatchers.Main).launch {
            val results = searchFilesFromDb(query)
            pluginResult(results, query)
            isProcessing = false
        }
    }

    private suspend fun searchFilesFromDb(query: String): List<ResultAdapter> {
        val results = mutableListOf<ResultAdapter>()

        withContext(Dispatchers.IO) {
            val files = dao.searchFiles(query.trim())
            files.map { file ->
                val uri = FileProvider.getUriForFile(
                    mContext,
                    "${mContext.packageName}.provider",
                    File(file.path)
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, file.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val thumbnail = async(Dispatchers.IO) {
                    getFileIconOrThumbnail(File(file.path))
                }

                results.add(
                    ResultAdapter(
                        file.name,
                        file.path.replace("/storage/emulated/0/", ""),
                        thumbnail.await(),
                        intent,
                        null
                    )
                )

            }
        }
        return results

    }


    private suspend fun refreshFileCache() {
        withContext(Dispatchers.IO) {
            if (!isExternalStorageReadable()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(mContext, "Storage permission is required to scan files.", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val fileList = mutableListOf<FileEntity>()
            val rootDir = Environment.getExternalStorageDirectory()
            scanFilesRecursive(rootDir, fileList)

            dao.clearFiles()
            dao.insertFiles(fileList)
        }
    }

    private fun scanFilesRecursive(dir: File, fileList: MutableList<FileEntity>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                scanFilesRecursive(file, fileList)
            } else {
                val mimeType = getMimeType(file)
                fileList.add(
                    FileEntity(
                        file.path,
                        file.name,
                        mimeType
                    )
                )
//                Log.d("FileSearchPlugin", "Added file: ${file.path}")
            }
        }
    }

    private fun getMimeType(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
            name.endsWith(".png") -> "image/png"
            name.endsWith(".pdf") -> "application/pdf"
            name.endsWith(".mp4") -> "video/mp4"
            name.endsWith(".mp3") -> "audio/mpeg"
            else -> "*/*"
        }
    }

    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY
    }

    private fun getFileIconOrThumbnail(file: File): Drawable? {
        val path = file.path
        val name = file.name.lowercase()

        if (thumbnailCache.containsKey(path)) return thumbnailCache[path]

        val thumbnail: Bitmap? = when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") -> {
                ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(path),
                    128, 128
                )
            }

            name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp") -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ThumbnailUtils.createVideoThumbnail(
                        file,
                        Size(128, 128),
                        null
                    )
                } else null
            }

            else -> null
        }

        if (thumbnail != null) {
            val bitmapDrawable = thumbnail.toDrawable(mContext.resources)
            thumbnailCache[path] = bitmapDrawable
            return bitmapDrawable
        }

        // Fallback to icon resource based on type
        val iconRes = when {
            name.endsWith(".pdf") -> R.drawable.baseline_calculate_24
            name.endsWith(".mp3") || name.endsWith(".wav") -> R.drawable.baseline_calculate_24
            name.endsWith(".zip") || name.endsWith(".rar") -> R.drawable.baseline_calculate_24
            file.isDirectory -> R.drawable.baseline_calculate_24
            else -> R.drawable.baseline_calculate_24 // generic file icon
        }

        return ContextCompat.getDrawable(mContext, iconRes)
    }
}