/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.local_database

import android.content.Context
import com.brave.playlist.model.DownloadQueueModel
import com.brave.playlist.model.PlaylistItemModel

class PlaylistRepository(context: Context) {

    private var playlistItemModelDao: PlaylistItemModelDao? =
        PlaylistDatabase.getInstance(context)?.playlistItemModelDao()

    fun getPlaylistItemById(playlistItemId: String): PlaylistItemModel? {
        return playlistItemModelDao?.getPlaylistItemById(playlistItemId)
    }

    fun insertPlaylistItemModel(playlistItemModel: PlaylistItemModel) {
        playlistItemModelDao?.insertPlaylistItemModel(playlistItemModel)
    }

    fun deleteAllPlaylistItemModel() {
        playlistItemModelDao?.deleteAllPlaylistItemModel()
    }

    fun getAllDownloadQueueModel() : List<DownloadQueueModel>? {
        return playlistItemModelDao?.getAllDownloadQueueModel()
    }

    fun insertDownloadQueueModel(downloadQueueModel: DownloadQueueModel) {
        playlistItemModelDao?.insertDownloadQueueModel(downloadQueueModel)
    }

    fun deleteAllDownloadQueueModel() {
        playlistItemModelDao?.deleteAllDownloadQueueModel()
    }

    fun deleteDownloadQueueModel(downloadQueueModel: DownloadQueueModel) {
        playlistItemModelDao?.deleteDownloadQueueModel(downloadQueueModel)
    }
}
