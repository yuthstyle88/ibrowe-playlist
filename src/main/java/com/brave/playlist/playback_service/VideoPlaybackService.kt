/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.playback_service

import android.app.PendingIntent
import android.content.Intent
import android.media.session.PlaybackState
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.brave.playlist.activity.PlaylistMenuOnboardingActivity
import com.brave.playlist.local_database.PlaylistRepository
import com.brave.playlist.model.LastPlayedPositionModel
import com.brave.playlist.model.PlaylistOnboardingModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.continuousListening
import com.brave.playlist.util.PlaylistPreferenceUtils.rememberFilePlaybackPosition
import com.brave.playlist.util.PlaylistPreferenceUtils.rememberListPlaybackPosition
import com.brave.playlist.util.PlaylistPreferenceUtils.setLatestPlaylistItem
import com.brave.playlist.util.PlaylistUtils
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi class VideoPlaybackService : MediaLibraryService() , MediaLibraryService.MediaLibrarySession.Callback, Player.Listener {
    private lateinit var mPlayer: ExoPlayer
    private lateinit var mMediaLibrarySession: MediaLibrarySession
    private val mScope = CoroutineScope(Job() + Dispatchers.IO)

    private val mPlaylistRepository: PlaylistRepository by lazy {
        PlaylistRepository(applicationContext)
    }

    companion object {
        private const val immutableFlag = PendingIntent.FLAG_IMMUTABLE

//        var CURRENTLY_PLAYED_ITEM_ID: String? = null
//        private val mutableCastStatus = MutableLiveData<Boolean>()
//        val castStatus: LiveData<Boolean> get() = mutableCastStatus
//        private fun setCastStatus(shouldShowControls: Boolean) {
//            mutableCastStatus.value = shouldShowControls
//        }

        private val mutableCurrentPlayingItem = MutableLiveData<String>()
        val currentPlayingItem: LiveData<String> get() = mutableCurrentPlayingItem
        private fun setCurrentPlayingItem(currentPlayingItemId: String) {
            mutableCurrentPlayingItem.value = currentPlayingItemId
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("NTP", "VideoPlaybackService")
        initializeSessionAndPlayer()
//        setListener(MediaSessionServiceListener())
        lastSavedPositionTimer()
    }

    private fun initializeSessionAndPlayer() {
//        val dataSourceFactory = DataSource.Factory {FileDataSource.Factory().createDataSource() }
//        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        mPlayer =
            ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
//                .setMediaSourceFactory(
//                    mediaSourceFactory
//                )
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_LOCAL)
                .build()
        mPlayer.addListener(this)

        mMediaLibrarySession =
            MediaLibrarySession.Builder(this, mPlayer, this)
//                .setSessionActivity(PendingIntent.getActivity(
//                    applicationContext,
//                    0,
//                    Intent(this, PlaylistMenuOnboardingActivity::class.java),
//                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//                ))
                .build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mMediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!mPlayer.playWhenReady || mPlayer.mediaItemCount == 0) {
            stopSelf()
        }
    }
    override fun onDestroy() {
        mMediaLibrarySession.release()
        mPlayer.release()
        clearListener()
        cancelLastSavedPositionTimer()
        super.onDestroy()
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        // We need to use URI from requestMetaData because of https://github.com/androidx/media/issues/282
        val updatedMediaItems: List<MediaItem> =
            mediaItems.map { mediaItem ->
                 MediaItem.Builder()
                    .setMediaId(mediaItem.mediaId)
                    .setRequestMetadata(mediaItem.requestMetadata)
                    .setMediaMetadata(mediaItem.mediaMetadata)
                    .setUri(mediaItem.requestMetadata.mediaUri)
                    .build()
            }
        return Futures.immediateFuture(updatedMediaItems)
    }

//    override fun onUpdateNotification(session: MediaSession) {
//        val mediaItem = mPlayer.currentMediaItem
//        createNotification(session, mediaItem) //calling method where we create notification
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun  createNotification(session: MediaSession, mediaItem: MediaItem?) {
//        val notificationManagerCompat = NotificationManagerCompat.from(this@VideoPlaybackService)
//        ensureNotificationChannel(notificationManagerCompat)
//
//        val pendingIntent = PendingIntent.getActivity(
//                    applicationContext,
//                    0,
//                    PlaylistUtils.playlistNotificationIntent(applicationContext),
//                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//                )
//
//        val builder =
//            NotificationCompat.Builder(this@VideoPlaybackService, PLAYLIST_CHANNEL_ID)
//                .setContentIntent(pendingIntent)
//                .setSmallIcon(R.drawable.ic_playing_sound)
//                .setLargeIcon(mediaItem?.mediaMetadata?.artworkUri?.let {
//                    Icon.createWithAdaptiveBitmapContentUri(
//                        it
//                    )
//                })
//                .setContentTitle(mediaItem?.mediaMetadata?.title)
//                .setContentText(mediaItem?.mediaMetadata?.artist)
//                .setStyle(MediaStyleNotificationHelper.MediaStyle(session).setShowActionsInCompactView(0,1,2))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
//        notificationManagerCompat.notify(PLAYLIST_NOTIFICATION_ID, builder.build())
//    }
//
//    private fun ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
//        if (Util.SDK_INT < 26 || notificationManagerCompat.getNotificationChannel(
//                PLAYLIST_CHANNEL_ID
//            ) != null
//        ) {
//            return
//        }
//
//        val channel =
//            NotificationChannel(
//                PLAYLIST_CHANNEL_ID,
//                "playlist",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//        channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
//        channel.setShowBadge(false)
//        notificationManagerCompat.createNotificationChannel(channel)
//    }


//
//    private inner class MediaSessionServiceListener : Listener {
//
//        /**
//         * This method is only required to be implemented on Android 12 or above when an attempt is made
//         * by a media controller to resume playback when the {@link MediaSessionService} is in the
//         * background.
//         */
//        @SuppressLint("MissingPermission") // TODO: b/280766358 - Request this permission at runtime.
//        override fun onForegroundServiceStartNotAllowedException() {
//            val notificationManagerCompat = NotificationManagerCompat.from(this@VideoPlaybackService)
//            ensureNotificationChannel(notificationManagerCompat)
//            val pendingIntent =
//                TaskStackBuilder.create(this@VideoPlaybackService).run {
//                    addNextIntent(Intent(this@VideoPlaybackService, PlaylistMenuOnboardingActivity::class.java))
//                    getPendingIntent(0, immutableFlag or PendingIntent.FLAG_UPDATE_CURRENT)
//                }
//            val builder =
//                NotificationCompat.Builder(this@VideoPlaybackService, PLAYLIST_CHANNEL_ID)
//                    .setContentIntent(pendingIntent)
//                    .setSmallIcon(R.drawable.ic_playing_sound)
//                    .setContentTitle("title")
//                    .setStyle(
//                        NotificationCompat.BigTextStyle().bigText("Text")
//                    )
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .setAutoCancel(true)
//            notificationManagerCompat.notify(PLAYLIST_NOTIFICATION_ID, builder.build())
//        }
//    }
//


    // Last saved position timer
    private var mLastSavedPositionHandler: Handler? = null
    private val mSavePositionRunnableCode: Runnable = object : Runnable {
        override fun run() {
            if (mPlayer.isPlaying) {
                Log.e(ConstantUtils.TAG, "runnableCode")
                mPlayer.currentMediaItem?.let { saveLastPosition(it, mPlayer.currentPosition) }
            }
            mLastSavedPositionHandler?.postDelayed(this, 2000)
        }
    }
    private fun lastSavedPositionTimer() {
        mLastSavedPositionHandler = Handler(mPlayer.applicationLooper)
        mLastSavedPositionHandler?.post(mSavePositionRunnableCode)
    }
    private fun cancelLastSavedPositionTimer() {
        mLastSavedPositionHandler?.removeCallbacks(mSavePositionRunnableCode)
    }


    // Player callbacks
    override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
        if (playbackState == Player.STATE_ENDED) {
            mPlayer.currentMediaItem?.let { saveLastPosition(it, 0) }
        }
//        updateCurrentItemIndex()
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
    }

    override fun onTimelineChanged(timeline: Timeline, reason: @Player.TimelineChangeReason Int) {
//        updateCurrentItemIndex()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        Log.e(
            ConstantUtils.TAG,
            VideoPlaybackService::class.java.name + " : onMediaItemTransition : Reason : " + reason
        )
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
//            mScope.launch {
//                val currentPlayingItem = mCurrentPlayer?.currentMediaItemIndex?.let {
//                    getMediaItemFromPosition(
//                        it
//                    )
//                }
//                currentPlayingItem?.id?.let {
//                    val downloadStatus = mPlaylistRepository.getDownloadQueueModelById(it)?.downloadStatus
//                    Log.e(TAG, "downloadStatus : $downloadStatus")
//                    when(downloadStatus) {
//                        DownloadStatus.PENDING.name -> {
//                            mCurrentPlayer?.pause()
//                        }
//                        DownloadStatus.DOWNLOADING.name -> {
//                            mCurrentPlayer?.pause()
//                        }
//                        DownloadStatus.DOWNLOADED.name -> {
//                            mCurrentPlayer?.prepare()
//                            mCurrentPlayer?.play()
//                        }
//                    }
//                }
//            }
        } else {
            mPlayer.playWhenReady =
                PlaylistPreferenceUtils.defaultPrefs(applicationContext).continuousListening
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: @Player.DiscontinuityReason Int
    ) {
        val playbackState = mPlayer.playbackState
        if (playbackState == PlaybackState.STATE_PLAYING) {
            Log.e(ConstantUtils.TAG, "onPositionDiscontinuity")
            val size = mPlayer.mediaItemCount
            val previousItemIndex =
                if (mPlayer.currentMediaItemIndex in 1 until size) (mPlayer.currentMediaItemIndex - 1) else mPlayer.currentMediaItemIndex
            saveLastPosition(mPlayer.getMediaItemAt(previousItemIndex), 0)
            updateCurrentlyPlayedItem()
        }
    }


    private fun saveLastPosition(mediaItem: MediaItem, currentPosition: Long) {
        mScope.launch {
            if (PlaylistPreferenceUtils.defaultPrefs(applicationContext).rememberFilePlaybackPosition) {
                mediaItem.mediaId.let{
                    val lastPlayedPositionModel =LastPlayedPositionModel(it, currentPosition)
                    mPlaylistRepository.insertLastPlayedPosition(lastPlayedPositionModel)
                }
            }
        }
        if (PlaylistPreferenceUtils.defaultPrefs(applicationContext).rememberListPlaybackPosition) {
            mediaItem.let{
                val playlistId = it.mediaMetadata.extras?.getString("playlist_id")?:""
                PlaylistPreferenceUtils.defaultPrefs(applicationContext)
                    .setLatestPlaylistItem(playlistId, it.mediaId)
            }
        }
        updateCurrentlyPlayedItem()
    }

    private fun updateCurrentlyPlayedItem() {
        mPlayer.currentMediaItem?.let{
            setCurrentPlayingItem(it.mediaId)
//            CURRENTLY_PLAYED_ITEM_ID = it.mediaId
        }
//        Log.e("CURRENTLY_PLAYED_ITEM_ID", VideoPlaybackService.CURRENTLY_PLAYED_ITEM_ID.toString())
    }
}
