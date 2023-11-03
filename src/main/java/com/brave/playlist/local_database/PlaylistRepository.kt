/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.local_database

import android.content.Context
import com.brave.playlist.model.DownloadQueueModel
import com.brave.playlist.model.LastPlayedPositionModel

class PlaylistRepository(context: Context) {

    private var mPlaylistItemModelDao: PlaylistItemModelDao? =
        PlaylistDatabase.getInstance(context)?.playlistItemModelDao()

//    fun getPlaylistItemById(playlistItemId: String): PlaylistItemModel? {
//        return playlistItemModelDao?.getPlaylistItemById(playlistItemId)
//    }
//
//    fun insertPlaylistItemModel(playlistItemModel: PlaylistItemModel) {
//        playlistItemModelDao?.insertPlaylistItemModel(playlistItemModel)
//    }
//
//    fun deleteAllPlaylistItemModel() {
//        playlistItemModelDao?.deleteAllPlaylistItemModel()
//    }

    fun getLastPlayedPositionByPlaylistItemId(playlistItemId: String): LastPlayedPositionModel? {
        return mPlaylistItemModelDao?.getLastPlayedPositionByPlaylistItemId(playlistItemId)
    }

    fun insertLastPlayedPosition(lastPlayedPositionModel: LastPlayedPositionModel) {
        mPlaylistItemModelDao?.insertLastPlayedPosition(lastPlayedPositionModel)
    }

    fun getDownloadQueueModelById(playlistItemId: String): DownloadQueueModel? {
        return mPlaylistItemModelDao?.getDownloadQueueModelById(playlistItemId)
    }

    fun isDownloadQueueModelExists(playlistItemId: String): Boolean? {
        return mPlaylistItemModelDao?.isDownloadQueueModelExists(playlistItemId)
    }

    fun updateDownloadQueueModel(downloadQueueModel: DownloadQueueModel) {
        mPlaylistItemModelDao?.updateDownloadQueueModel(downloadQueueModel)
    }

    fun getFirstDownloadQueueModel(): DownloadQueueModel? {
        return mPlaylistItemModelDao?.getFirstDownloadQueueModel()
    }

    fun getAllDownloadQueueModel(): List<DownloadQueueModel>? {
        return mPlaylistItemModelDao?.getAllDownloadQueueModel()
    }

    fun insertDownloadQueueModel(downloadQueueModel: DownloadQueueModel) {
        mPlaylistItemModelDao?.insertDownloadQueueModel(downloadQueueModel)
    }

    fun deleteAllDownloadQueueModel() {
        mPlaylistItemModelDao?.deleteAllDownloadQueueModel()
    }

    fun deleteDownloadQueueModel(downloadQueueModel: DownloadQueueModel) {
        mPlaylistItemModelDao?.deleteDownloadQueueModel(downloadQueueModel)

    }
}
