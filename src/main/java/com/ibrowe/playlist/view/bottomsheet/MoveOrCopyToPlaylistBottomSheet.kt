/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.ibrowe.playlist.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibrowe.playlist.PlaylistViewModel
import com.ibrowe.playlist.R
import com.ibrowe.playlist.adapter.recyclerview.PlaylistAdapter
import com.ibrowe.playlist.enums.PlaylistOptionsEnum
import com.ibrowe.playlist.extension.setTopCornersRounded
import com.ibrowe.playlist.fragment.AllPlaylistFragment
import com.ibrowe.playlist.fragment.NewPlaylistFragment
import com.ibrowe.playlist.listener.PlaylistClickListener
import com.ibrowe.playlist.model.MoveOrCopyModel
import com.ibrowe.playlist.model.PlaylistModel
import com.ibrowe.playlist.util.ConstantUtils
import com.ibrowe.playlist.util.PlaylistUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class MoveOrCopyToPlaylistBottomSheet :
    BottomSheetDialogFragment(), PlaylistClickListener {

    private lateinit var mPlaylistViewModel: PlaylistViewModel
    private val mMoveOrCopyModel: MoveOrCopyModel = PlaylistUtils.moveOrCopyModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_add_or_move_to_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPlaylistViewModel = ViewModelProvider(requireActivity())[PlaylistViewModel::class.java]

        val layoutBottomSheet: MaterialCardView = view.findViewById(R.id.layoutBottomSheet)
        layoutBottomSheet.setTopCornersRounded(16)

        var fromPlaylistId = ""
        if (mMoveOrCopyModel.playlistItems.isNotEmpty()) {
            fromPlaylistId = mMoveOrCopyModel.playlistItems[0].playlistId
        }

        mPlaylistViewModel.fetchPlaylistData(ConstantUtils.ALL_PLAYLIST)

        mPlaylistViewModel.allPlaylistData.observe(viewLifecycleOwner) { allPlaylistData ->
            val allPlaylistList = mutableListOf<PlaylistModel>()
            for (allPlaylistModel in allPlaylistData) {
                if (allPlaylistModel.id != fromPlaylistId) {
                    allPlaylistList.add(
                        PlaylistModel(
                            allPlaylistModel.id,
                            allPlaylistModel.name,
                            allPlaylistModel.items
                        )
                    )
                }
            }

            allPlaylistList.add(
                0,
                PlaylistModel(
                    ConstantUtils.NEW_PLAYLIST,
                    getString(R.string.playlist_new_text),
                    arrayListOf()
                )
            )

            val rvPlaylists: RecyclerView = view.findViewById(R.id.rvPlaylists)
            rvPlaylists.layoutManager = LinearLayoutManager(view.context)
            val playlistAdapter = PlaylistAdapter(this)
            rvPlaylists.adapter = playlistAdapter
            playlistAdapter.submitList(allPlaylistList)
        }

        val behavior = BottomSheetBehavior.from(layoutBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onPlaylistClick(playlistModel: PlaylistModel) {
        if (playlistModel.id == ConstantUtils.NEW_PLAYLIST) {
            PlaylistUtils.moveOrCopyModel =
                MoveOrCopyModel(
                    mMoveOrCopyModel.playlistOptionsEnum,
                    "",
                    mMoveOrCopyModel.playlistItems
                )
            val newPlaylistFragment = NewPlaylistFragment.newInstance(
                PlaylistOptionsEnum.NEW_PLAYLIST,
                shouldMoveOrCopy = true
            )
            parentFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, newPlaylistFragment)
                .addToBackStack(AllPlaylistFragment::class.simpleName)
                .commit()
        } else {
            PlaylistUtils.moveOrCopyModel = MoveOrCopyModel(
                mMoveOrCopyModel.playlistOptionsEnum,
                playlistModel.id,
                mMoveOrCopyModel.playlistItems
            )
            mPlaylistViewModel.performMoveOrCopy(PlaylistUtils.moveOrCopyModel)
        }
        dismiss()
    }
}
