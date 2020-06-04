package com.appypie.video.app.ui.home

import android.os.Bundle
import android.view.View
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseFragment
import kotlinx.android.synthetic.main.content_room.*
import kotlinx.android.synthetic.main.joining_text_layout.*

class VideoRoomFragment(private val videoRoomInitializer: VideoRoomInitializer) : BaseFragment() {


    override fun layoutRes(): Int {
        return R.layout.room_video_layout
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoRoomInitializer.onVideoLayoutInitialized(primary_video, remote_video_thumbnails, join_status)
    }


}