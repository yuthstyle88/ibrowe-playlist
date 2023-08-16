/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.util

import android.content.Context
import android.net.Uri
import com.brave.playlist.model.PlaylistItemModel
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist.Segment
import com.google.android.exoplayer2.source.hls.playlist.HlsMultivariantPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser
import java.io.File
import java.io.FileInputStream

object HLSParsingUtil {
    @JvmStatic
    fun getContentManifestUrl(context: Context, playlistItemModel: PlaylistItemModel): String {
        var contentManifestUrl = ""
        val hlsParser =
            context.contentResolver?.openInputStream(Uri.parse(playlistItemModel.mediaPath))
                ?.let {
                    HlsPlaylistParser().parse(
                        Uri.parse(playlistItemModel.mediaSrc),
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
    ): List<Segment>? {
        var contentSegments:List<Segment> ? = null
        val hlsParser = HlsPlaylistParser().parse(
            Uri.parse(baseUrl), FileInputStream(
                File(contentManifestFilePath)
            )
        )
        if (hlsParser is HlsMediaPlaylist) {
            contentSegments =  hlsParser.segments
        }
        return contentSegments
    }
}
