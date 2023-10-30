/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.fragment

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.brave.playlist.R
import com.brave.playlist.playback_service.VideoPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

@SuppressLint("UnsafeOptInUsageError")
class PlayerFragment : Fragment(R.layout.fragment_playlist_player){
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private lateinit var playerView: PlayerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerView = view.findViewById(R.id.styledPlayerView)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeController()
    }

    override fun onDetach() {
        playerView.player = null
        releaseController()
        super.onDetach()
    }

    private fun initializeController() {
        controllerFuture =
            MediaController.Builder(
                requireContext(),
                SessionToken(requireContext(), ComponentName(requireContext(), VideoPlaybackService::class.java))
            )
                .buildAsync()
        controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }

    private fun setController() {
        val controller = this.controller ?: return
        playerView.player = controller
    }
}
