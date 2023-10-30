/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.local_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.brave.playlist.model.DownloadQueueModel
import com.brave.playlist.model.LastPlayedPositionModel
import com.brave.playlist.model.PlaylistItemModel

@Dao
interface PlaylistItemModelDao {
//    @Query("SELECT * FROM PlaylistItemModel")
//    fun getAll(): List<PlaylistItemModel>
//
//    @Query("SELECT * FROM PlaylistItemModel WHERE id = :playlistItemId LIMIT 1")
//    fun getPlaylistItemById(playlistItemId: String): PlaylistItemModel
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertPlaylistItemModel(vararg playlistItemModel: PlaylistItemModel)

//    @Query("SELECT * FROM PlaylistItemModel")
//    fun getAll(): List<PlaylistItemModel>

    @Query("SELECT * FROM LastPlayedPositionModel WHERE playlist_item_id = :playlistItemId LIMIT 1")
    fun getLastPlayedPositionByPlaylistItemId(playlistItemId: String): LastPlayedPositionModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLastPlayedPosition(vararg lastPlayedPositionModel: LastPlayedPositionModel)

//    @Update
//    fun updatePlaylistItemModel(vararg playlistItemModel: PlaylistItemModel)
//
//    @Delete
//    fun deletePlaylistItemModel(vararg playlistItemModel: PlaylistItemModel)
//
//    @Query("DELETE FROM PlaylistItemModel")
//    fun deleteAllPlaylistItemModel()


    // Download queue models

    @Query("SELECT * FROM DownloadQueueModel")
    fun getAllDownloadQueueModel(): List<DownloadQueueModel>

    @Query("SELECT * FROM DownloadQueueModel WHERE download_status = 'PENDING' LIMIT 1")
    fun getFirstDownloadQueueModel(): DownloadQueueModel

    @Query("SELECT * FROM DownloadQueueModel WHERE playlist_item_id = :playlistItemId LIMIT 1")
    fun getDownloadQueueModelById(playlistItemId: String): DownloadQueueModel

    @Query("SELECT EXISTS (SELECT 1 FROM DownloadQueueModel WHERE playlist_item_id = :playlistItemId)")
    fun isDownloadQueueModelExists(playlistItemId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDownloadQueueModel(vararg downloadQueueModel: DownloadQueueModel)

    @Update
    fun updateDownloadQueueModel(vararg downloadQueueModel: DownloadQueueModel)

    @Delete
    fun deleteDownloadQueueModel(vararg downloadQueueModel: DownloadQueueModel)

    @Query("DELETE FROM DownloadQueueModel")
    fun deleteAllDownloadQueueModel()

}
