package com.example.v2

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object AppInfoResolver {

    fun getAppName(
        context: Context,
        packageName: String
    ): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val label = appInfo.loadLabel(pm)?.toString()

            if (!label.isNullOrBlank()) label else packageName
        } catch (e: Exception) {
            packageName
        }
    }


    fun getAppIconBitmap(
        context: Context,
        packageName: String
    ): ImageBitmap? {
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            if (drawable is BitmapDrawable) {
                drawable.bitmap.asImageBitmap()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}




