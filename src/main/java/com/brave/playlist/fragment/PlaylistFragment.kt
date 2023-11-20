/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.fragment

/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.recyclerview.PlaylistItemAdapter
import com.brave.playlist.enums.DownloadStatus
import com.brave.playlist.enums.PlaylistOptionsEnum
import com.brave.playlist.extension.afterMeasured
import com.brave.playlist.listener.ItemInteractionListener
import com.brave.playlist.listener.PlaylistItemClickListener
import com.brave.playlist.listener.PlaylistItemOptionsListener
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.listener.StartDragListener
import com.brave.playlist.local_database.PlaylistRepository
import com.brave.playlist.model.DownloadQueueModel
import com.brave.playlist.model.MoveOrCopyModel
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistItemOptionModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.playback_service.VideoPlaybackService
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.ConstantUtils.DEFAULT_PLAYLIST
import com.brave.playlist.util.ConstantUtils.TAG
import com.brave.playlist.util.MediaItemUtil
import com.brave.playlist.util.MediaUtils
import com.brave.playlist.util.MenuUtils
import com.brave.playlist.util.PlaylistItemGestureHelper
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.getLatestPlaylistItem
import com.brave.playlist.util.PlaylistPreferenceUtils.recentlyPlayedPlaylist
import com.brave.playlist.util.PlaylistPreferenceUtils.rememberListPlaybackPosition
import com.brave.playlist.util.PlaylistUtils
import com.brave.playlist.view.PlaylistToolbar
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.LinkedList

class PlaylistFragment : Fragment(R.layout.fragment_playlist), ItemInteractionListener,
    StartDragListener, PlaylistOptionsListener, PlaylistItemOptionsListener,
    PlaylistItemClickListener {

    private lateinit var mBrowserFuture: ListenableFuture<MediaBrowser>
    private val mMediaBrowser: MediaBrowser?
        get() = if (mBrowserFuture.isDone) mBrowserFuture.get() else null

    private val mScope = CoroutineScope(Job() + Dispatchers.IO)

    private lateinit var mPlaylistModel: PlaylistModel
    private lateinit var mPlaylistViewModel: PlaylistViewModel
    private var mPlaylistItemAdapter: PlaylistItemAdapter? = null
    private lateinit var mPlaylistToolbar: PlaylistToolbar
    private lateinit var mRvPlaylist: RecyclerView
    private lateinit var mTvTotalMediaCount: AppCompatTextView
    private lateinit var mTvPlaylistName: AppCompatTextView
    private lateinit var mLayoutPlayMedia: LinearLayoutCompat
    private lateinit var mIvPlaylistOptions: AppCompatImageView
    private lateinit var mProgressBar: ContentLoadingProgressBar
    private lateinit var mLayoutShuffleMedia: LinearLayoutCompat
    private lateinit var mIvPlaylistCover: AppCompatImageView
    private lateinit var mTvPlaylistTotalSize: AppCompatTextView
    private lateinit var mItemTouchHelper: ItemTouchHelper

    private lateinit var mEmptyView: View
    private lateinit var mPlaylistView: View

    private val mPlaylistRepository: PlaylistRepository by lazy {
        PlaylistRepository(requireContext())
    }

    private fun initializeBrowser() {
        mBrowserFuture =
            MediaBrowser.Builder(
                requireContext(),
                SessionToken(
                    requireContext(),
                    ComponentName(requireContext(), VideoPlaybackService::class.java)
                )
            )
                .buildAsync()
        mBrowserFuture.addListener({ }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(mBrowserFuture)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeBrowser()
    }

    override fun onDetach() {
        super.onDetach()
        releaseBrowser()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val hlsParser = HlsPlaylistParser().parse(Uri.parse("https://res.cloudinary.com/dannykeane/video/upload/sp_full_hd/q_80:qmax_90,ac_none/v1/dk-memoji-dark.m3u8"), FileInputStream(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath+"/"+"dk-memoji-dark.m3u8")))
//
//        var hlsParser2: HlsPlaylist? = null
//        if (hlsParser is HlsMultivariantPlaylist && hlsParser.variants.size > 0) {
//            Log.e(TAG, hlsParser.mediaPlaylistUrls.toString())
//            hlsParser2 = HlsPlaylistParser().parse(Uri.parse(hlsParser.variants[0].url.toString()), FileInputStream(
//                File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath+"/"+"dk-memoji-dark_1.m3u8")
//            )
//            )
//            if (hlsParser2 is HlsMediaPlaylist) {
//                Log.e(TAG, hlsParser2.segments[0].url)
//            }
//        }
//
//        Uri.parse(UriUtil.resolve("https://prodamdnewsencoding.akamaized.net/out/v1/6847e0355d7b47fdab9571f575a1eac5/43b6f121beb24ffaa1509325e7e23fb2/15bb94d4cae942ed8a198cc8f63db8ed/4e78157149424d08a27f6290f287f72f/f8fdd6ff3a2a47d6ad0e7c243092b4e7/index_1.m3u8", "../../../4e78157149424d08a27f6290f287f72f/f8fdd6ff3a2a47d6ad0e7c243092b4e7/index_1_0.ts"))


        mPlaylistViewModel = ViewModelProvider(requireActivity())[PlaylistViewModel::class.java]

        mEmptyView = view.findViewById(R.id.empty_view)
        mPlaylistView = view.findViewById(R.id.playlist_view)

        mPlaylistToolbar = view.findViewById(R.id.playlistToolbar)
//        mPlaylistToolbar.setOptionsButtonClickListener {
//            if (activity is AppCompatActivity) (activity as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
//        }
        mPlaylistToolbar.setExitEditModeClickListener {
            mPlaylistItemAdapter?.setEditMode(false)
            mPlaylistToolbar.updateSelectedItems(0)
            mPlaylistToolbar.enableEditMode(false)
            mIvPlaylistOptions.visibility = View.VISIBLE
            //Reorder list
            mPlaylistItemAdapter?.currentList
                ?.let { playlistItems -> mPlaylistViewModel.reorderPlaylistItems(playlistItems) }
        }
        mPlaylistToolbar.setMoveClickListener { actionView ->
            mPlaylistItemAdapter?.getSelectedItems()?.let {
                if (it.size > 0) {
                    MenuUtils.showMoveOrCopyMenu(
                        actionView, parentFragmentManager, it, this@PlaylistFragment
                    )
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.playlist_please_select_items),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        mPlaylistToolbar.setDeleteClickListener {
            mPlaylistItemAdapter?.getSelectedItems()?.let {
                if (it.size > 0) {
                    for (selectedItem in it) {
                        stopVideoPlayerOnDelete(selectedItem)
                    }
                    mPlaylistViewModel.setDeletePlaylistItems(
                        PlaylistModel(
                            mPlaylistModel.id, mPlaylistModel.name, it
                        )
                    )
                    mPlaylistItemAdapter?.setEditMode(false)
                    mPlaylistToolbar.enableEditMode(false)
                    mIvPlaylistOptions.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.playlist_please_select_items),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        mRvPlaylist = view.findViewById(R.id.rvPlaylists)
        mTvTotalMediaCount = view.findViewById(R.id.tvTotalMediaCount)
        mTvPlaylistName = view.findViewById(R.id.tvPlaylistName)
        mLayoutPlayMedia = view.findViewById(R.id.layoutPlayMedia)
        mLayoutPlayMedia.isVisible
        mLayoutShuffleMedia = view.findViewById(R.id.layoutShuffleMedia)
        mIvPlaylistOptions = view.findViewById(R.id.ivPlaylistOptions)
        mIvPlaylistCover = view.findViewById(R.id.ivPlaylistCover)
        mTvPlaylistTotalSize = view.findViewById(R.id.tvPlaylistTotalSize)
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar.visibility = View.VISIBLE
        mRvPlaylist.visibility = View.GONE

        VideoPlaybackService.currentPlayingItem.observe(viewLifecycleOwner) { currentPlayingItemId ->
            if (!currentPlayingItemId.isNullOrEmpty()) {
                mPlaylistItemAdapter?.updatePlayingStatus(currentPlayingItemId)
            }
        }

        mPlaylistViewModel.playlistData.observe(viewLifecycleOwner) { playlistData ->
            Log.e(TAG, playlistData.toString())
            playlistData.items.forEach {
                Log.e("reorder : playlistData", it.name)
            }
            var totalFileSize = 0L
            mPlaylistModel = playlistData
            mIvPlaylistOptions.setImageResource(if (mPlaylistModel.id == DEFAULT_PLAYLIST) R.drawable.ic_edit_playlist else R.drawable.ic_options_toolbar_playlist)

            view.findViewById<AppCompatButton>(R.id.btBrowseForMedia).setOnClickListener {
                requireActivity().finish()
            }

            if (mPlaylistModel.items.isNotEmpty()) {
                Glide.with(requireContext()).load(mPlaylistModel.items[0].thumbnailPath)
                    .placeholder(R.drawable.ic_playlist_placeholder)
                    .error(R.drawable.ic_playlist_placeholder).into(mIvPlaylistCover)
                mLayoutPlayMedia.setOnClickListener {
                    if (PlaylistPreferenceUtils.defaultPrefs(requireContext()).rememberListPlaybackPosition && !TextUtils.isEmpty(
                            PlaylistPreferenceUtils.defaultPrefs(requireContext())
                                .getLatestPlaylistItem(mPlaylistModel.id)
                        )
                    ) {
                        mPlaylistModel.items.forEachIndexed { index, playlistToOpen ->
                            if (playlistToOpen.id == PlaylistPreferenceUtils.defaultPrefs(
                                    requireContext()
                                ).getLatestPlaylistItem(mPlaylistModel.id)
                            ) {
                                openPlaylistPlayer(false, index)
                                return@forEachIndexed
                            }
                        }
                    } else {
                        openPlaylistPlayer(false, 0)
                    }
                }

                mLayoutShuffleMedia.setOnClickListener {
                    openPlaylistPlayer(true, (0 until mPlaylistModel.items.size).shuffled().first())
                }

                mTvTotalMediaCount.text = getString(
                    R.string.playlist_number_of_items, mPlaylistModel.items.size.toString()
                )

                mTvPlaylistName.text =
                    if (mPlaylistModel.id == DEFAULT_PLAYLIST) resources.getString(R.string.playlist_play_later) else mPlaylistModel.name

                if (activity is AppCompatActivity) {
                    (activity as AppCompatActivity).onBackPressedDispatcher.addCallback(
                        requireActivity(),
                        object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                if (mPlaylistItemAdapter?.getEditMode() == true) {
                                    mPlaylistItemAdapter?.setEditMode(false)
                                    mPlaylistToolbar.enableEditMode(false)
                                    mIvPlaylistOptions.visibility = View.VISIBLE
                                    //Reorder list
                                    mPlaylistItemAdapter?.currentList
                                        ?.let { mPlaylistViewModel.reorderPlaylistItems(it) }
                                } else {
                                    this.remove()
                                    (activity as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        })
                }

                mScope.launch {
                    mPlaylistModel.items.forEach {
                        if (it.isCached) {
                            totalFileSize += it.mediaFileBytes
                        }
                    }

                    mPlaylistModel.items.forEach { playlistItemModel ->
                        val isDownloadQueueModelExists =
                            mPlaylistRepository.isDownloadQueueModelExists(playlistItemModel.id)
                                ?: false
                        if (playlistItemModel.isCached && MediaUtils.isHlsFile(playlistItemModel.mediaPath) && !isDownloadQueueModelExists) {
                            mPlaylistRepository.insertDownloadQueueModel(
                                DownloadQueueModel(
                                    playlistItemModel.id, DownloadStatus.PENDING.name
                                )
                            )
                        }
                    }
                    try {
                        val hlsDownloadServiceClass =
                            Class.forName("org.chromium.chrome.browser.playlist.download.DownloadService")
                        if (mPlaylistRepository.getAllDownloadQueueModel()
                                ?.isNotEmpty() == true && !PlaylistUtils.isServiceRunning(
                                requireContext(), hlsDownloadServiceClass
                            )
                        ) {
                            requireContext().startService(
                                Intent(
                                    requireContext(), hlsDownloadServiceClass
                                )
                            )
                        }
                    } catch (ex: ClassNotFoundException) {
                        Log.e(TAG, "hlsDownloadServiceClass" + ex.message)
                    }

                    activity?.runOnUiThread {
                        if (totalFileSize > 0) {
                            mTvPlaylistTotalSize.text =
                                Formatter.formatShortFileSize(view.context, totalFileSize)
                        }
                        mPlaylistItemAdapter = PlaylistItemAdapter(
                            this@PlaylistFragment, this@PlaylistFragment
                        )
                        mPlaylistItemAdapter?.submitList(mPlaylistModel.items.toMutableList())
                        mPlaylistItemAdapter?.let {
                            val callback = PlaylistItemGestureHelper(
                                view.context, mRvPlaylist, it, this@PlaylistFragment
                            )
                            mItemTouchHelper = ItemTouchHelper(callback)
                            mItemTouchHelper.attachToRecyclerView(mRvPlaylist)
                        }
                        mRvPlaylist.adapter = mPlaylistItemAdapter
                        mPlaylistItemAdapter?.setEditMode(false)
                        mPlaylistToolbar.enableEditMode(false)
                        mIvPlaylistOptions.visibility = View.VISIBLE
                        mPlaylistView.visibility = View.VISIBLE
                        mEmptyView.visibility = View.GONE

                        mProgressBar.visibility = View.GONE
                        mRvPlaylist.visibility = View.VISIBLE

                        mPlaylistViewModel.downloadProgress.observe(viewLifecycleOwner) {
                            mPlaylistItemAdapter?.updatePlaylistItemDownloadProgress(it)
                        }

                        PlaylistUtils.downloadProgress.observe(viewLifecycleOwner) {
                            mPlaylistItemAdapter?.updatePlaylistItemDownloadProgress(it)
                        }

                        mPlaylistViewModel.playlistItemUpdate.observe(viewLifecycleOwner) {
                            mPlaylistItemAdapter?.updatePlaylistItem(it)
                            mPlaylistModel.items.forEach { currentPlaylistItemModel ->
                                val currentMediaFileBytes =
                                    if (currentPlaylistItemModel.id == it.id) {
                                        it.mediaFileBytes
                                    } else {
                                        currentPlaylistItemModel.mediaFileBytes
                                    }
                                if (currentPlaylistItemModel.isCached) {
                                    totalFileSize += currentMediaFileBytes
                                }
                            }
                            mTvPlaylistTotalSize.text =
                                Formatter.formatShortFileSize(view.context, totalFileSize)
                        }

//                        VideoPlaybackService.newPlaylistItemModel.observe(viewLifecycleOwner) {
//                            mPlaylistItemAdapter?.updatePlaylistItem(it)
//                            mPlaylistModel.items.forEach { currentPlaylistItemModel ->
//                                val currentMediaFileBytes =
//                                    if (currentPlaylistItemModel.id == it.id) {
//                                        it.mediaFileBytes
//                                    } else {
//                                        currentPlaylistItemModel.mediaFileBytes
//                                    }
//                                if (currentPlaylistItemModel.isCached) {
//                                    totalFileSize += currentMediaFileBytes
//                                }
//                            }
//                            mTvPlaylistTotalSize.text =
//                                Formatter.formatShortFileSize(view.context, totalFileSize)
//                        }

                        mRvPlaylist.afterMeasured {
                            mMediaBrowser?.currentMediaItem?.mediaId?.let {
                                mPlaylistItemAdapter?.updatePlayingStatus(
                                    it
                                )
                            }
                            if (arguments?.getBoolean(ConstantUtils.SHOULD_OPEN_PLAYER) == true && mPlaylistModel.items.isNotEmpty()) {
                                val currentPlaylistItemId = mMediaBrowser?.currentMediaItem?.mediaId
                                mPlaylistModel.items.forEachIndexed { index, item ->
                                    if (item.id == currentPlaylistItemId) {
                                        Log.e(TAG, item.id + " : " + item.name)
                                        openPlaylistPlayer(false, index)
                                        arguments?.putBoolean(ConstantUtils.SHOULD_OPEN_PLAYER, false)
                                        return@forEachIndexed
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                setEmptyView()
            }
        }

        mIvPlaylistOptions.setOnClickListener {
            if (mPlaylistModel.id == DEFAULT_PLAYLIST) {
                mPlaylistItemAdapter?.setEditMode(true)
                mPlaylistToolbar.enableEditMode(true)
                mIvPlaylistOptions.visibility = View.GONE
            } else {
                MenuUtils.showPlaylistMenu(
                    view.context,
                    parentFragmentManager,
                    mPlaylistModel,
                    this@PlaylistFragment,
                    mPlaylistModel.id == DEFAULT_PLAYLIST
                )
            }
        }
    }

    private fun setEmptyView() {
        mIvPlaylistCover.setImageResource(R.drawable.ic_playlist_placeholder)
        mEmptyView.visibility = View.VISIBLE
        mPlaylistView.visibility = View.GONE
    }

    override fun onItemDelete(position: Int) {
        val selectedPlaylistItem = mPlaylistItemAdapter?.currentList?.get(position)
        selectedPlaylistItem?.let {
            stopVideoPlayerOnDelete(it)
            mPlaylistViewModel.setDeletePlaylistItems(
                PlaylistModel(
                    mPlaylistModel.id, mPlaylistModel.name, arrayListOf(
                        it
                    )
                )
            )
        }
    }

    override fun onRemoveFromOffline(position: Int) {
        val selectedPlaylistItem = mPlaylistItemAdapter?.currentList?.get(position)
        selectedPlaylistItem?.let {
            stopVideoPlayerOnDelete(it)
            val playlistOptionsEnumModel = PlaylistItemOptionModel(
                requireContext().resources.getString(R.string.playlist_delete_item_offline_data),
                R.drawable.ic_remove_offline_data_playlist,
                PlaylistOptionsEnum.DELETE_ITEMS_OFFLINE_DATA,
                playlistItemModel = it,
                playlistId = mPlaylistModel.id
            )
            mPlaylistViewModel.setPlaylistItemOption(playlistOptionsEnumModel)
        }
    }

    override fun onShare(position: Int) {
        super.onShare(position)
        val playlistItemModel = mPlaylistModel.items[position]
        //Share model
        PlaylistUtils.showSharingDialog(requireContext(), playlistItemModel.pageSource)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper.startDrag(viewHolder)
    }

//    override fun onPlaylistItemClick(playlistItemModel: PlaylistItemModel) {
//        openPlaylistPlayer(false, playlistItemModel)
//    }

    override fun onPlaylistItemClick(position: Int) {
//        mPlaylistToolbar.updateSelectedItems(position)
        openPlaylistPlayer(false, position)
    }

    override fun onPlaylistItemClickInEditMode(count: Int) {
        mPlaylistToolbar.updateSelectedItems(count)
    }

    override fun onPlaylistItemMenuClick(view: View, playlistItemModel: PlaylistItemModel) {
        MenuUtils.showPlaylistItemMenu(
            view.context,
            parentFragmentManager,
            playlistItemModel = playlistItemModel,
            playlistId = playlistItemModel.playlistId,
            playlistItemOptionsListener = this@PlaylistFragment,
            mPlaylistModel.name == DEFAULT_PLAYLIST
        )
    }

    private fun openPlaylistPlayer(isShuffle: Boolean, position: Int) {
//        if (!selectedPlaylistItemModel.isCached && PlaylistUtils.isMediaSourceExpired(
//                selectedPlaylistItemModel.mediaSrc
//            )
//        ) {
//            Toast.makeText(
//                requireContext(),
//                getString(R.string.playlist_item_expired_message),
//                Toast.LENGTH_SHORT
//            ).show()
//            val playlistItemOptionModel = PlaylistItemOptionModel(
//                requireContext().resources.getString(R.string.playlist_open_in_private_tab),
//                R.drawable.ic_private_tab,
//                PlaylistOptionsEnum.RECOVER_PLAYLIST_ITEM,
//                playlistItemModel = selectedPlaylistItemModel,
//                playlistId = selectedPlaylistItemModel.playlistId
//            )
//            mPlaylistViewModel.setPlaylistItemOption(playlistItemOptionModel)
//        } else

        Log.e("openPlaylistPlayer", ": 1 : ")

        val browser = this.mMediaBrowser ?: return

        var recentPlaylistIds = LinkedList<String>()
        val recentPlaylistJson =
            PlaylistPreferenceUtils.defaultPrefs(requireContext()).recentlyPlayedPlaylist
        if (!recentPlaylistJson.isNullOrEmpty()) {
            recentPlaylistIds = GsonBuilder().serializeNulls().create().fromJson(
                recentPlaylistJson,
                TypeToken.getParameterized(LinkedList::class.java, String::class.java).type
            )
            if (recentPlaylistIds.contains(mPlaylistModel.id)) {
                recentPlaylistIds.remove(mPlaylistModel.id)
            }
        }
        recentPlaylistIds.addFirst(mPlaylistModel.id)
        PlaylistPreferenceUtils.defaultPrefs(requireContext()).recentlyPlayedPlaylist =
            GsonBuilder().serializeNulls().create().toJson(recentPlaylistIds)

        Log.e("openPlaylistPlayer", ": 2 : ")
//        if (PlaylistUtils.isServiceRunning(requireContext(), VideoPlaybackService::class.java)) {
//            activity?.stopService(Intent(requireContext(), VideoPlaybackService::class.java))
//        }

//        if (!PlaylistUtils.isServiceRunning(requireContext(), VideoPlaybackService::class.java)) {
        val subItemMediaList = mutableListOf<MediaItem>()
        mPlaylistModel.items.forEach {
            if (PlaylistUtils.isPlaylistItemCached(it)) {
                val mediaItem = MediaItemUtil.buildMediaItem(
                    it,
                    mPlaylistModel.id,
                    if (mPlaylistModel.id == DEFAULT_PLAYLIST) resources.getString(R.string.playlist_play_later) else mPlaylistModel.name,
                )
                subItemMediaList.add(mediaItem)
            }
        }

        Log.e("openPlaylistPlayer", ": 3 : ")

        mScope.launch {
            Log.e("openPlaylistPlayer", ": 4 : ")
            val selectedPlaylistItem = mPlaylistModel.items[position]
            val lastPlayedPositionModel =
                mPlaylistRepository.getLastPlayedPositionByPlaylistItemId(selectedPlaylistItem.id)

            activity?.runOnUiThread {
                Log.e("openPlaylistPlayer", ": 5 : ")
//                browser.setMediaItems(
//                    subItemMediaList,
//                    position,
//                     0
//                )
                browser.clearMediaItems()
                browser.addMediaItems(subItemMediaList)
                browser.seekTo(position, lastPlayedPositionModel?.lastPlayedPosition?:0)
                browser.shuffleModeEnabled = isShuffle
                browser.prepare()
                browser.play()
                Log.e("openPlaylistPlayer", ": 6 : ")
//                browser.sessionActivity?.send()
            }
        }
//        }

        mPlaylistModel.items.forEach {
            Log.e("reorder : mPlaylistModel", it.name)
        }
        val playlistPlayerFragment = PlaylistPlayerFragment.newInstance(mPlaylistModel)
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, playlistPlayerFragment)
            .addToBackStack(PlaylistFragment::class.simpleName).commit()

//        }
    }

    override fun onPlaylistOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        when (playlistOptionsModel.optionType) {
            PlaylistOptionsEnum.EDIT_PLAYLIST -> {
                mPlaylistItemAdapter?.setEditMode(true)
                mPlaylistToolbar.enableEditMode(true)
                mIvPlaylistOptions.visibility = View.GONE
            }

            PlaylistOptionsEnum.MOVE_PLAYLIST_ITEMS, PlaylistOptionsEnum.COPY_PLAYLIST_ITEMS -> {
                mPlaylistItemAdapter?.getSelectedItems()?.let {
                    PlaylistUtils.moveOrCopyModel = MoveOrCopyModel(
                        playlistOptionsModel.optionType, "", it
                    )
                }
                mPlaylistItemAdapter?.setEditMode(false)
                mPlaylistToolbar.enableEditMode(false)
                mIvPlaylistOptions.visibility = View.VISIBLE
            }

            PlaylistOptionsEnum.RENAME_PLAYLIST -> {
                val newPlaylistFragment = NewPlaylistFragment.newInstance(
                    PlaylistOptionsEnum.RENAME_PLAYLIST, mPlaylistModel
                )
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, newPlaylistFragment)
                    .addToBackStack(PlaylistFragment::class.simpleName).commit()
            }

            PlaylistOptionsEnum.DELETE_PLAYLIST -> {
//                activity?.stopService(Intent(requireContext(), VideoPlaybackService::class.java))
                mMediaBrowser?.stop()
                if (activity is AppCompatActivity) (activity as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
            }

            else -> {
                //Do nothing
            }
        }
        mPlaylistViewModel.setPlaylistOption(playlistOptionsModel)
    }

    override fun onPlaylistItemOptionClicked(playlistItemOptionModel: PlaylistItemOptionModel) {
        if (playlistItemOptionModel.optionType == PlaylistOptionsEnum.SHARE_PLAYLIST_ITEM) {
            playlistItemOptionModel.playlistItemModel?.pageSource?.let {
                PlaylistUtils.showSharingDialog(
                    requireContext(), it
                )
            }
        } else {
            if (playlistItemOptionModel.optionType == PlaylistOptionsEnum.MOVE_PLAYLIST_ITEM || playlistItemOptionModel.optionType == PlaylistOptionsEnum.COPY_PLAYLIST_ITEM) {
                val moveOrCopyItems = ArrayList<PlaylistItemModel>()
                playlistItemOptionModel.playlistItemModel?.let { moveOrCopyItems.add(it) }
                PlaylistUtils.moveOrCopyModel =
                    MoveOrCopyModel(playlistItemOptionModel.optionType, "", moveOrCopyItems)
            } else if (playlistItemOptionModel.optionType == PlaylistOptionsEnum.DELETE_ITEMS_OFFLINE_DATA || playlistItemOptionModel.optionType == PlaylistOptionsEnum.DELETE_PLAYLIST_ITEM) {
                playlistItemOptionModel.playlistItemModel?.let { stopVideoPlayerOnDelete(it) }
            }
            mPlaylistViewModel.setPlaylistItemOption(playlistItemOptionModel)
        }
    }

    private fun stopVideoPlayerOnDelete(selectedPlaylistItem: PlaylistItemModel) {
        mMediaBrowser?.currentMediaItem?.mediaId?.let {
            if (it == selectedPlaylistItem.id) {
//                activity?.stopService(Intent(requireContext(), VideoPlaybackService::class.java))
                mMediaBrowser?.stop()
            }
        }
    }
}
