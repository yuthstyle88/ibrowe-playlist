package com.brave.playlist.enums

import androidx.annotation.Keep

@Keep
enum class PlaylistOptions {
    //Playlist button options
    ADD_MEDIA,
    OPEN_PLAYLIST,
    PLAYLIST_SETTINGS,
    PLAYLIST_HIDE,

    // All playlist options
    REMOVE_ALL_OFFLINE_DATA,
    DOWNLOAD_ALL_PLAYLISTS_FOR_OFFLINE_USE,

    // Playlist options
    EDIT_PLAYLIST,
    RENAME_PLAYLIST,
    REMOVE_PLAYLIST_OFFLINE_DATA,
    DOWNLOAD_PLAYLIST_FOR_OFFLINE_USE,
    DELETE_PLAYLIST,

    // Playlist item options
    MOVE_PLAYLIST_ITEM,
    COPY_PLAYLIST_ITEM,
    DELETE_ITEMS_OFFLINE_DATA,
    SHARE_PLAYLIST_ITEM,
    OPEN_IN_NEW_TAB,
    OPEN_IN_PRIVATE_TAB,
    DELETE_PLAYLIST_ITEM,
    RECOVER_PLAYLIST_ITEM,

    // Playlist multiple items options
    MOVE_PLAYLIST_ITEMS,
    COPY_PLAYLIST_ITEMS,
    DELETE_PLAYLIST_ITEMS,

    // Extra options
    NEW_PLAYLIST
}