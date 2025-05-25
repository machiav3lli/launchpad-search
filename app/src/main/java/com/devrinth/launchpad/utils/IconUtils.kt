package com.devrinth.launchpad.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object IconUtils {
    fun drawableToByteArray(drawable: Drawable): ByteArray {

        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            is AdaptiveIconDrawable -> {
                val background = drawable.background
                val foreground = drawable.foreground

                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)

                background?.setBounds(0, 0, width, height)
                background?.draw(canvas)

                foreground?.setBounds(0, 0, width, height)
                foreground?.draw(canvas)

                bitmap
            }
            else -> {
                // fallback for unknown drawables
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
                bitmap
            }
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun byteArrayToDrawable(context: Context, byteArray: ByteArray): Drawable {
        return BitmapDrawable(context.resources, BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
    }

    fun base64ToDrawable(context: Context, base64String: String): Drawable? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val inputStream = ByteArrayInputStream(decodedBytes)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                BitmapDrawable(context.resources, bitmap)
            } else {
                println("Error: Could not decode Base64 string to Bitmap.")
                null
            }
        } catch (e: IllegalArgumentException) {
            println("Error: Invalid Base64 string - ${e.message}")
            null
        }
    }
}
