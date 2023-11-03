/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.adapter.recyclerview

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.listener.PlaylistItemClickListener
import com.brave.playlist.listener.StartDragListener
import com.brave.playlist.model.DownloadProgressModel
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.util.PlaylistUtils
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.CircularProgressIndicator


class PlaylistItemAdapter(
    private val playlistItemClickListener: PlaylistItemClickListener?,
    private val startDragListener: StartDragListener? = null,
) : AbstractRecyclerViewAdapter<PlaylistItemModel, PlaylistItemAdapter.MediaItemViewHolder>() {
    private var editMode = false
    private var isBottomLayout = false

    init {
        editMode = false
    }

    private var allViewHolderViews = HashMap<String, View>()
    fun updatePlaylistItemDownloadProgress(downloadProgressModel: DownloadProgressModel) {
        val view = allViewHolderViews[downloadProgressModel.playlistItemId]
//        val ivMediaStatus: AppCompatImageView? = view?.findViewById(R.id.ivMediaStatus)
        val tvMediaDownloadProgress: AppCompatTextView? =
            view?.findViewById(R.id.tvMediaDownloadProgress)
        val processingProgressBar: CircularProgressIndicator? =
            view?.findViewById(R.id.processing_progress_bar)
        if (downloadProgressModel.totalBytes != downloadProgressModel.receivedBytes) {
//            ivMediaStatus?.visibility = View.GONE
            tvMediaDownloadProgress?.visibility = View.VISIBLE
            processingProgressBar?.visibility = View.VISIBLE
//            processingProgressBar?.alpha = 1.0f
            processingProgressBar?.setProgressCompat(
                downloadProgressModel.receivedBytes.toInt(),
                true
            )
            processingProgressBar?.max = downloadProgressModel.totalBytes.toInt()
//            tvMediaDownloadProgress?.text =
//                view?.resources?.getString(R.string.playlist_percentage_text)
//                    ?.let { String.format(it, downloadProgressModel.percentComplete) }
            tvMediaDownloadProgress?.text =
                view?.resources?.getString(R.string.playlist_preparing_text)
        } else {
            tvMediaDownloadProgress?.visibility = View.GONE
            processingProgressBar?.visibility = View.GONE
        }
    }

    fun updatePlaylistItem(playlistItemModel: PlaylistItemModel) {
        val currentPlaylistItems = ArrayList<PlaylistItemModel>()
        currentList.forEach {
            if (it.id == playlistItemModel.id) {
                playlistItemModel.playlistId = it.playlistId
                currentPlaylistItems.add(playlistItemModel)
            } else {
                currentPlaylistItems.add(it)
            }
        }
        submitList(currentPlaylistItems)
    }

    fun List<PlaylistItemModel>.replace(
        newValue: PlaylistItemModel,
        block: (String) -> Boolean
    ): List<PlaylistItemModel> {
        return map {
            if (block(it.id)) {
                newValue.playlistId = it.playlistId
                newValue
            } else {
                it
            }
        }
    }

    fun updatePlayingStatus(playlistItemId: String) {
        if (getEditMode()) {
            return
        }
        val currentPlayingItemView = allViewHolderViews[playlistItemId]
        val ivMediaPlayingStatusCurrent: AppCompatImageView? =
            currentPlayingItemView?.findViewById(R.id.ivMediaPlayingStatus)
        ivMediaPlayingStatusCurrent?.visibility = View.VISIBLE
        val mediaTitleCurrent: AppCompatTextView? =
            currentPlayingItemView?.findViewById(R.id.tvMediaTitle)
        mediaTitleCurrent?.setTextColor(currentPlayingItemView.context.getColor(R.color.brave_theme_color))
        allViewHolderViews.keys.forEach {
            if (it != playlistItemId) {
                val view = allViewHolderViews[it]
                val ivMediaPlayingStatus: AppCompatImageView? =
                    view?.findViewById(R.id.ivMediaPlayingStatus)
                ivMediaPlayingStatus?.visibility = View.GONE
                val mediaTitle: AppCompatTextView? = view?.findViewById(R.id.tvMediaTitle)
                mediaTitle?.setTextColor(view.context.getColor(R.color.playlist_text_color))
            }
        }
    }

    fun setEditMode(enable: Boolean) {
        editMode = enable
        currentList.forEach { it.isSelected = false }
        notifyItemRangeChanged(0, itemCount)
    }

    fun getEditMode(): Boolean {
        return editMode
    }

    fun setBottomLayout() {
        isBottomLayout = true
    }

    inner class MediaItemViewHolder(view: View) : AbstractViewHolder<PlaylistItemModel>(view) {
        private val ivMediaThumbnail: AppCompatImageView
        private val tvMediaTitle: AppCompatTextView
        private val tvMediaDuration: AppCompatTextView
        private val tvMediaFileSize: AppCompatTextView
        private val ivDragMedia: AppCompatImageView
        private val ivMediaOptions: AppCompatImageView
        private val ivMediaSelected: AppCompatImageView

        //        private val ivMediaStatus: AppCompatImageView
        private val tvMediaDownloadProgress: AppCompatTextView

        init {
            ivMediaThumbnail = view.findViewById(R.id.ivMediaThumbnail)
            tvMediaTitle = view.findViewById(R.id.tvMediaTitle)
            tvMediaDuration = view.findViewById(R.id.tvMediaDuration)
            tvMediaFileSize = view.findViewById(R.id.tvMediaFileSize)
            ivDragMedia = view.findViewById(R.id.ivDragMedia)
            ivMediaOptions = view.findViewById(R.id.ivMediaOptions)
            ivMediaSelected = view.findViewById(R.id.ivMediaSelected)
//            ivMediaStatus = view.findViewById(R.id.ivMediaStatus)
            tvMediaDownloadProgress = view.findViewById(R.id.tvMediaDownloadProgress)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBind(position: Int, model: PlaylistItemModel) {
            setViewOnSelected(model.isSelected)
//            val downloadIcon =
//                if (PlaylistUtils.isPlaylistItemCached(model)) {
//                    R.drawable.ic_downloaded
//                } else {
//                    R.drawable.ic_offline
//                }
//            ivMediaStatus.setImageResource(downloadIcon)
//            ivMediaStatus.visibility = if (!editMode) View.VISIBLE else View.GONE
            if (PlaylistUtils.isPlaylistItemCached(model)) {
                setAlphaForViews(itemView as ViewGroup, 1.0f)
            } else {
                setAlphaForViews(itemView as ViewGroup, 0.4f)
            }
            tvMediaTitle.text = model.name
            if (PlaylistUtils.isPlaylistItemCached(model)) {
                tvMediaDownloadProgress.visibility = View.GONE
            }

            if (model.thumbnailPath.isNotEmpty()) {
                Glide.with(itemView.context).asBitmap()
                    .placeholder(R.drawable.ic_playlist_item_placeholder)
                    .error(R.drawable.ic_playlist_item_placeholder).load(model.thumbnailPath)
                    .into(ivMediaThumbnail)
            } else {
                ivMediaThumbnail.setImageResource(R.drawable.ic_playlist_item_placeholder)
            }

            if (model.isCached) {
                val fileSize = model.mediaFileBytes
                tvMediaFileSize.text = Formatter.formatShortFileSize(itemView.context, fileSize)
            }

            tvMediaFileSize.visibility = if (model.isCached) View.VISIBLE else View.GONE

            if (model.duration.isNotEmpty()) {
                val duration = model.duration.toLongOrNull()
                if (duration != null && duration > 0) {
                    val milliseconds = (duration / 1000) % 1000
                    val seconds = ((duration / 1000) - milliseconds) / 1000 % 60
                    val minutes = (((duration / 1000) - milliseconds) / 1000 - seconds) / 60 % 60
                    val hours =
                        ((((duration / 1000) - milliseconds) / 1000 - seconds) / 60 - minutes) / 60

                    val hourTime: String = if (hours > 0) itemView.context.resources.getString(
                        R.string.playlist_time_text, hours.toString()
                    ) else ""
                    val minuteTime: String = if (minutes > 0) itemView.context.resources.getString(
                        R.string.playlist_time_text, minutes.toString()
                    ) else ""
                    tvMediaDuration.visibility = View.VISIBLE
                    tvMediaDuration.text = itemView.context.resources.getString(
                        R.string.playlist_duration_text, hourTime, minuteTime, seconds.toString()
                    )
                }
            }
            ivMediaOptions.visibility = if (!editMode) View.VISIBLE else View.GONE
            ivMediaOptions.setOnClickListener {
                if (!PlaylistUtils.isPlaylistItemCached(model)) {
                    return@setOnClickListener
                }
                playlistItemClickListener?.onPlaylistItemMenuClick(
                    view = it, playlistItemModel = model
                )
            }
            ivDragMedia.visibility = if (editMode) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                if (editMode) {
                    model.isSelected = !model.isSelected
                    setViewOnSelected(model.isSelected)
                    var count = 0
                    currentList.forEach {
                        if (it.isSelected) {
                            count++
                        }
                    }
                    playlistItemClickListener?.onPlaylistItemClickInEditMode(count)
                } else {
                    if (!PlaylistUtils.isPlaylistItemCached(model)) {
                        return@setOnClickListener
                    }
                    playlistItemClickListener?.onPlaylistItemClick(position)

                }
            }
            ivDragMedia.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) startDragListener?.onStartDrag(
                    this
                )
                false
            }

            allViewHolderViews[model.id] = itemView
        }

        override fun isSelected(position: Int): Boolean {
            return position in 0 until itemCount && currentList[position].isSelected
        }

        private fun setViewOnSelected(isSelected: Boolean) {
            ivMediaSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
            itemView.setBackgroundColor(if (isSelected) itemView.context.getColor(R.color.selected_media) else Color.TRANSPARENT)
        }
    }

    private fun setAlphaForViews(parentLayout: ViewGroup, alphaValue: Float) {
        for (i in 0 until parentLayout.childCount) {
            val child: View = parentLayout.getChildAt(i)

            // Check if the child is the layout you want to exclude
            if (child.id != R.id.processing_progress_bar) { // Replace with your exclusion layout's ID
                child.alpha = alphaValue
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_item_layout, parent, false)
        return MediaItemViewHolder(view)
    }

    fun getSelectedItems(): ArrayList<PlaylistItemModel> {
        val selectedItems = arrayListOf<PlaylistItemModel>()
        currentList.forEach {
            if (it.isSelected) {
                selectedItems.add(it)
            }
        }
        return selectedItems
    }
}
