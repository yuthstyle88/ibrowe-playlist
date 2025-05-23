/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.ibrowe.playlist.view

import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.ibrowe.playlist.R
import com.ibrowe.playlist.adapter.PlaylistOnboardingFragmentStateAdapter
import com.ibrowe.playlist.extension.addScrimBackground
import com.ibrowe.playlist.extension.afterMeasured
import com.ibrowe.playlist.extension.showOnboardingGradientBg
import com.ibrowe.playlist.listener.PlaylistOnboardingActionClickListener
import com.ibrowe.playlist.util.PlaylistUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class PlaylistOnboardingPanel(
    fragmentActivity: FragmentActivity,
    anchorView: View,
    playlistOnboardingActionClickListener: PlaylistOnboardingActionClickListener
) {

    init {
        val view = View.inflate(fragmentActivity, R.layout.panel_playlist_onboarding, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(view, width, height, true)

        val onboardingLayout = view.findViewById<LinearLayoutCompat>(R.id.onboardingLayout)
        onboardingLayout.afterMeasured {
            showOnboardingGradientBg()
        }

        val playlistOnboardingViewPager: ViewPager2 =
            view.findViewById(R.id.playlistOnboardingViewPager)

        val adapter = PlaylistOnboardingFragmentStateAdapter(
            fragmentActivity, PlaylistUtils.getOnboardingItemList(context = view.context)
        )
        playlistOnboardingViewPager.adapter = adapter

        val nextButton: AppCompatButton = view.findViewById(R.id.btNextOnboarding)
        nextButton.setOnClickListener {
            if (playlistOnboardingViewPager.currentItem == 2) {
                playlistOnboardingActionClickListener.onOnboardingActionClick()
                popupWindow.dismiss()
            } else {
                playlistOnboardingViewPager.currentItem =
                    playlistOnboardingViewPager.currentItem + 1
            }
        }

        playlistOnboardingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                nextButton.text =
                    if (position == 2) fragmentActivity.getString(R.string.playlist_try_it) else fragmentActivity.getString(
                        R.string.playlist_next
                    )
                adapter.notifyItemChanged(position)
            }
        })

        val tabLayout: TabLayout = view.findViewById(R.id.playlistOnboardingTabLayout)
        TabLayoutMediator(tabLayout, playlistOnboardingViewPager) { tab, _ ->
            tab.setIcon(R.drawable.ic_tab_layout_dot_selector)
        }.attach()

        view.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        popupWindow.animationStyle = R.style.OnboardingWindowAnimation

        val y = anchorView.top - view.measuredHeight
        val x = anchorView.left - view.measuredWidth - 20

        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(anchorView, Gravity.TOP, x, y)
        popupWindow.addScrimBackground()
    }
}
