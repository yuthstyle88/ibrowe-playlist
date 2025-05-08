/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import java.io.File
import java.io.FileInputStream
import java.util.LinkedList
import java.util.Queue

@SuppressLint("UnsafeOptInUsageError")
object HLSParsingUtil {
    @JvmStatic
    fun getContentManifestUrl(
        context: Context,
        baseUrl: String,
        localManifestFilePath: String
    ): String {
        var contentManifestUrl = ""
        val hlsParser =
            context.contentResolver?.openInputStream(Uri.parse(localManifestFilePath))
                ?.let {
                    HlsPlaylistParser().parse(
                        Uri.parse(baseUrl),
                        it
                    )
                }
        if (hlsParser != null && hlsParser is HlsMultivariantPlaylist) {
            contentManifestUrl = hlsParser.variants[0].url.toString()
        }
        return contentManifestUrl
    }

    @JvmStatic
    fun getContentSegments(
        contentManifestFilePath: String,
        baseUrl: String
    ): Queue<HlsMediaPlaylist.Segment> {
        val contentSegments: Queue<HlsMediaPlaylist.Segment> = LinkedList()
        val hlsParser = HlsPlaylistParser().parse(
            Uri.parse(baseUrl), FileInputStream(
                File(contentManifestFilePath)
            )
        )
        if (hlsParser is HlsMediaPlaylist) {
            contentSegments.addAll(hlsParser.segments)
        }
        return contentSegments
    }
}
