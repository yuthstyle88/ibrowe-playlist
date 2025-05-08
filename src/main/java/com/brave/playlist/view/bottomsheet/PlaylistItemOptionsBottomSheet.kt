/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.R
import com.brave.playlist.adapter.bottomsheet.PlaylistItemOptionsBottomSheetAdapter
import com.brave.playlist.extension.setTopCornersRounded
import com.brave.playlist.listener.PlaylistItemOptionsListener
import com.brave.playlist.model.PlaylistItemOptionModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class PlaylistItemOptionsBottomSheet(
    private val playlistItemOptionModel: MutableList<PlaylistItemOptionModel>,
    private val playlistItemOptionsListener: PlaylistItemOptionsListener
) :
    BottomSheetDialogFragment(), PlaylistItemOptionsListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_playlist_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutBottomSheet: MaterialCardView = view.findViewById(R.id.layoutBottomSheet)
        layoutBottomSheet.setTopCornersRounded(16)

        val rvBottomSheet: RecyclerView = view.findViewById(R.id.rvBottomSheet)
        rvBottomSheet.layoutManager = LinearLayoutManager(view.context)
        rvBottomSheet.adapter = PlaylistItemOptionsBottomSheetAdapter(playlistItemOptionModel, this)

        val behavior = BottomSheetBehavior.from(layoutBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false

        dialog?.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)
    }

    override fun onOptionClicked(playlistItemOptionModel: PlaylistItemOptionModel) {
        playlistItemOptionsListener.onOptionClicked(playlistItemOptionModel)
        dismiss()
    }
}
