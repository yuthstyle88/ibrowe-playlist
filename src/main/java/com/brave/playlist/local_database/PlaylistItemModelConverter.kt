/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.local_database

import androidx.room.TypeConverter
import com.brave.playlist.model.PlaylistItemModel
import com.google.gson.GsonBuilder

class PlaylistItemModelConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun playlistItemModelToString(playlistItemModel: PlaylistItemModel): String =
            GsonBuilder().create().toJson(playlistItemModel)

        @TypeConverter
        @JvmStatic
        fun stringToPlaylistItemModel(value: String): PlaylistItemModel {
            return GsonBuilder().create().fromJson(value, PlaylistItemModel::class.java)
        }
    }
}
