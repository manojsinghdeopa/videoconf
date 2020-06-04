package com.appypie.video.app.ui.home;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appypie.video.app.ui.room.ParticipantPrimaryView;
import com.appypie.video.app.ui.room.ParticipantPrimaryView;

public interface VideoRoomInitializer {

    void onVideoLayoutInitialized(ParticipantPrimaryView primaryVideoView, ViewGroup thumbnailLinearLayout, TextView joinStatusTextView);
}
