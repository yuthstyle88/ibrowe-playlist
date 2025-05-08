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
import com.brave.playlist.util.ConstantUtils.HLS_FILE_EXTENSION
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


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
    fun isHlsFile(mediaPath : String) : Boolean{
        val extension: String = mediaPath
            .substring(mediaPath.lastIndexOf("."))
        Log.e(ConstantUtils.TAG, "extension : $extension")
        return extension == HLS_FILE_EXTENSION
    }
}
