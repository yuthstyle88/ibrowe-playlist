/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


object MediaUtils {
    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var fileSize = 0L
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bytes = ByteArray(1024)
                var read: Int
                while (inputStream.read(bytes).also { read = it } >= 0) {
                    fileSize += read.toLong()
                }
            }
        } catch (ex: Exception) {
            Log.e(ConstantUtils.TAG, ex.message.toString())
        } finally {
            inputStream?.close()
        }
        return fileSize
    }

    @JvmStatic
    fun writeToFile(data: ByteArray?, filePath:String) {
        val file = File(filePath)
        data?.let { file.appendBytes(it) }
    }

    @JvmStatic
    fun getTempFile(context: Context): File {
        val outputDir =
            File(Environment.getExternalStorageDirectory(),Environment.DIRECTORY_DOWNLOADS) // context being the Activity pointer
        val file =  File(outputDir, "index.mp4")
        Log.e("data_source","path :"+ file.absolutePath)
        Log.e("data_source","canWrite :"+ file.canWrite())
        Log.e("data_source","canRead :"+ file.canRead())
        return file
    }
}
