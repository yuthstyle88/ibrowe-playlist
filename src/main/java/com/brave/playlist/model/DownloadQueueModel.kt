/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brave.playlist.enums.DownloadStatus
import com.google.gson.annotations.SerializedName

@Entity
data class DownloadQueueModel(
    @PrimaryKey @SerializedName("playlist_item_id") @ColumnInfo(name = "playlist_item_id") var playlistItemId: String,
//    @SerializedName("playlist_item_model") @ColumnInfo(name = "playlist_item_model") var playlistItemModel: PlaylistItemModel?,
    @SerializedName("download_status") @ColumnInfo(name = "download_status") var downloadStatus: String = DownloadStatus.PENDING.name
) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DownloadQueueModel> {
            override fun createFromParcel(parcel: Parcel) = DownloadQueueModel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<DownloadQueueModel>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        playlistItemId = parcel.readString().toString(),
//        playlistItemModel = parcel.readParcelable(PlaylistItemModel::class.java.classLoader),
        downloadStatus = parcel.readString().toString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(playlistItemId)
//        parcel.writeParcelable(playlistItemModel, PARCELABLE_WRITE_RETURN_VALUE)
        parcel.writeString(downloadStatus)
    }

    override fun describeContents() = 0
}
