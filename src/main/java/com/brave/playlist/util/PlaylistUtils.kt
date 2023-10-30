/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.util

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brave.playlist.R
import com.brave.playlist.activity.PlaylistMenuOnboardingActivity
import com.brave.playlist.local_database.PlaylistRepository
import com.brave.playlist.model.DownloadProgressModel
import com.brave.playlist.model.DownloadQueueModel
import com.brave.playlist.model.MoveOrCopyModel
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistOnboardingModel
import com.brave.playlist.util.ConstantUtils.PLAYLIST_CHANNEL_ID
import java.util.Date


object PlaylistUtils {
    @JvmStatic
    lateinit var moveOrCopyModel: MoveOrCopyModel
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                PLAYLIST_CHANNEL_ID,
                context.resources.getString(R.string.playlist_feature_text),
                NotificationManager.IMPORTANCE_HIGH
            )
            serviceChannel.lightColor = Color.BLUE
            serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(serviceChannel)
        }
    }

    fun isMediaSourceExpired(mediaSrc: String): Boolean {
        val uri: Uri =
            Uri.parse(mediaSrc)
        if (!uri.getQueryParameter("expire").isNullOrEmpty()) {
            val expireMillis: Long? = uri.getQueryParameter("expire")?.toLong()?.times(1000L)
            return Date() > expireMillis?.let { Date(it) }
        }
        return false
    }

    fun showSharingDialog(context: Context, text: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(
            Intent.createChooser(
                intent,
                context.resources.getString(R.string.playlist_share_with)
            )
        )
    }

    fun playlistNotificationIntent(
        context: Context,
        playlistItemModel: PlaylistItemModel
    ): Intent? {
        return try {
            val intent = Intent(
                context,
                Class.forName("org.chromium.chrome.browser.playlist.PlaylistHostActivity")
            )
            intent.action = ConstantUtils.PLAYLIST_ACTION
            intent.putExtra(ConstantUtils.CURRENT_PLAYING_ITEM_ID, playlistItemModel.id)
            intent.putExtra(ConstantUtils.CURRENT_PLAYLIST_ID, playlistItemModel.playlistId)
            intent.putExtra(ConstantUtils.PLAYLIST_NAME, playlistItemModel.name)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } catch (ex: ClassNotFoundException) {
            Log.e(ConstantUtils.TAG, "playlistNotificationIntent" + ex.message)
            null
        }
    }

    fun playlistNotificationIntent(
        context: Context
    ): Intent? {
        return try {
            val intent = Intent(
                context,
                Class.forName("org.chromium.chrome.browser.playlist.PlaylistHostActivity")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } catch (ex: ClassNotFoundException) {
            Log.e(ConstantUtils.TAG, "playlistNotificationIntent" + ex.message)
            null
        }
    }

    fun getOnboardingItemList(context: Context): List<PlaylistOnboardingModel> {
        return listOf(
            PlaylistOnboardingModel(
                context.getString(R.string.playlist_onboarding_title_1),
                context.getString(R.string.playlist_onboarding_text_1),
                R.drawable.ic_playlist_graphic_1
            ),
            PlaylistOnboardingModel(
                context.getString(R.string.playlist_onboarding_title_2),
                context.getString(R.string.playlist_onboarding_text_2),
                R.drawable.ic_playlist_graphic_2
            ),
            PlaylistOnboardingModel(
                context.getString(R.string.playlist_onboarding_title_3),
                context.getString(R.string.playlist_onboarding_text_3),
                R.drawable.ic_playlist_graphic_3
            )
        )
    }

    @JvmStatic
    fun openPlaylistMenuOnboardingActivity(context: Context) {
        val playlistActivityIntent = Intent(context, PlaylistMenuOnboardingActivity::class.java)
        playlistActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(playlistActivityIntent)
    }

    @JvmStatic
    fun openBraveActivityWithUrl(activity: ComponentActivity, url: String) {
        try {
            val intent =
                Intent(activity, Class.forName("org.chromium.chrome.browser.ChromeTabbedActivity"))
            intent.putExtra(ConstantUtils.OPEN_URL, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            activity.finish()
            activity.startActivity(intent)
        } catch (ex: ClassNotFoundException) {
            Log.e(ConstantUtils.TAG, "openBraveActivityWithUrl : " + ex.message)
        }
    }

    @JvmStatic
    fun insertDownloadQueue(context: Context, downloadQueueModel: DownloadQueueModel?) {
        downloadQueueModel?.let { PlaylistRepository(context).insertDownloadQueueModel(it) }
    }

    @JvmStatic
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.runningAppProcesses

        if (runningServices != null) {
            for (processInfo in runningServices) {
                if (processInfo.processName == serviceClass.name) {
                    return true
                }
            }
        }

        return false
    }

    @JvmStatic
    fun isPlaylistItemCached(selectedPlaylistItemModel : PlaylistItemModel) : Boolean {
        return selectedPlaylistItemModel.isCached && (!MediaUtils.isHlsFile(selectedPlaylistItemModel.mediaPath) || (MediaUtils.isHlsFile(
            selectedPlaylistItemModel.mediaPath
        ) && !TextUtils.isEmpty(selectedPlaylistItemModel.hlsMediaPath)))
    }

    private val mutableDownloadProgress = MutableLiveData<DownloadProgressModel>()
    val downloadProgress: LiveData<DownloadProgressModel> get() = mutableDownloadProgress
    @JvmStatic
    fun updateDownloadProgress(downloadProgressModel: DownloadProgressModel) {
        mutableDownloadProgress.value = downloadProgressModel
    }
}
