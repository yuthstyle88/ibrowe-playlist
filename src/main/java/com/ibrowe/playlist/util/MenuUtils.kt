/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.ibrowe.playlist.util

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import com.ibrowe.playlist.R
import com.ibrowe.playlist.enums.PlaylistOptionsEnum
import com.ibrowe.playlist.listener.PlaylistItemOptionsListener
import com.ibrowe.playlist.listener.PlaylistOptionsListener
import com.ibrowe.playlist.model.PlaylistItemModel
import com.ibrowe.playlist.model.PlaylistItemOptionModel
import com.ibrowe.playlist.model.PlaylistModel
import com.ibrowe.playlist.model.PlaylistOptionsModel
import com.ibrowe.playlist.view.bottomsheet.PlaylistItemOptionsBottomSheet
import com.ibrowe.playlist.view.bottomsheet.PlaylistOptionsBottomSheet

object MenuUtils {
    @JvmStatic
    fun showPlaylistItemMenu(
        context: Context,
        fragmentManager: FragmentManager,
        playlistItemModel: PlaylistItemModel,
        playlistId: String?,
        playlistItemOptionsListener: PlaylistItemOptionsListener,
        shouldShowMove: Boolean = true,
    ) {
        val optionsList: MutableList<PlaylistItemOptionModel> = mutableListOf()

        if (shouldShowMove) {
            optionsList.add(
                PlaylistItemOptionModel(
                    context.resources.getString(R.string.playlist_move_item),
                    R.drawable.ic_move_media,
                    PlaylistOptionsEnum.MOVE_PLAYLIST_ITEM,
                    playlistItemModel = playlistItemModel,
                    playlistId = playlistId
                )
            )
        }
        optionsList.add(
            PlaylistItemOptionModel(
                context.resources.getString(R.string.playlist_copy_item),
                R.drawable.ic_copy_media,
                PlaylistOptionsEnum.COPY_PLAYLIST_ITEM,
                playlistItemModel = playlistItemModel,
                playlistId = playlistId
            )
        )
        optionsList.add(
            PlaylistItemOptionModel(
                context.resources.getString(R.string.playlist_share_item),
                R.drawable.ic_share,
                PlaylistOptionsEnum.SHARE_PLAYLIST_ITEM,
                playlistItemModel = playlistItemModel,
                playlistId = playlistId
            )
        )
        optionsList.add(
            PlaylistItemOptionModel(
                context.resources.getString(R.string.playlist_open_in_new_tab),
                R.drawable.ic_new_tab,
                PlaylistOptionsEnum.OPEN_IN_NEW_TAB,
                playlistItemModel = playlistItemModel,
                playlistId = playlistId
            )
        )
        optionsList.add(
            PlaylistItemOptionModel(
                context.resources.getString(R.string.playlist_open_in_private_tab),
                R.drawable.ic_private_tab,
                PlaylistOptionsEnum.OPEN_IN_PRIVATE_TAB,
                playlistItemModel = playlistItemModel,
                playlistId = playlistId
            )
        )
        optionsList.add(
            PlaylistItemOptionModel(
                context.resources.getString(R.string.playlist_delete_item),
                R.drawable.ic_playlist_delete,
                PlaylistOptionsEnum.DELETE_PLAYLIST_ITEM,
                playlistItemModel = playlistItemModel,
                playlistId = playlistId
            )
        )
        PlaylistItemOptionsBottomSheet(
            optionsList, playlistItemOptionsListener
        ).show(fragmentManager, null)
    }

    @JvmStatic
    fun showMoveOrCopyMenu(
        view: View,
        fragmentManager: FragmentManager,
        selectedItems: ArrayList<PlaylistItemModel>,
        playlistOptionsListener: PlaylistOptionsListener
    ) {
        PlaylistOptionsBottomSheet(
            mutableListOf(
                PlaylistOptionsModel(
                    view.resources.getString(R.string.playlist_move_item),
                    R.drawable.ic_move_media,
                    PlaylistOptionsEnum.MOVE_PLAYLIST_ITEMS,
                    playlistItemModels = selectedItems
                ), PlaylistOptionsModel(
                    view.resources.getString(R.string.playlist_copy_item),
                    R.drawable.ic_copy_media,
                    PlaylistOptionsEnum.COPY_PLAYLIST_ITEMS,
                    playlistItemModels = selectedItems
                )
            ), playlistOptionsListener
        ).show(fragmentManager, null)
    }

    @JvmStatic
    fun showPlaylistMenu(
        context: Context,
        fragmentManager: FragmentManager,
        playlistModel: PlaylistModel,
        playlistOptionsListener: PlaylistOptionsListener,
        isDefaultPlaylist: Boolean = false
    ) {

        val optionsList: MutableList<PlaylistOptionsModel> = mutableListOf()
        optionsList.add(
            PlaylistOptionsModel(
                context.resources.getString(R.string.playlist_edit_text),
                R.drawable.ic_edit_playlist,
                PlaylistOptionsEnum.EDIT_PLAYLIST,
                playlistModel = playlistModel
            )
        )
        if (!isDefaultPlaylist) {
            optionsList.add(
                PlaylistOptionsModel(
                    context.resources.getString(R.string.playlist_rename_text),
                    R.drawable.ic_rename_playlist,
                    PlaylistOptionsEnum.RENAME_PLAYLIST,
                    playlistModel = playlistModel
                )
            )
        }
        if (!isDefaultPlaylist) {
            optionsList.add(
                PlaylistOptionsModel(
                    context.resources.getString(R.string.playlist_delete_playlist),
                    R.drawable.ic_playlist_delete,
                    PlaylistOptionsEnum.DELETE_PLAYLIST,
                    playlistModel = playlistModel
                )
            )
        }
        PlaylistOptionsBottomSheet(
            optionsList, playlistOptionsListener
        ).show(fragmentManager, null)
    }
}
