package com.appypie.video.app.ui.userHome;

import com.appypie.video.app.ui.home.VideoRoomFragment;
import com.appypie.video.app.ui.joinMeeting.AfterJoinMeeting;
import com.appypie.video.app.ui.room.ParticipantFragment;
import com.appypie.video.app.ui.room.RoomFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class UserHomeActivityBindingModule {


    @ContributesAndroidInjector
    abstract AfterJoinMeeting provideAfterJoinMeeting();


    @ContributesAndroidInjector
    abstract RoomFragment provideRoomFragment();


    @ContributesAndroidInjector
    abstract ParticipantFragment provideParticipantFragment();


    @ContributesAndroidInjector
    abstract VideoRoomFragment provideVideoRoomFragment();


    @ContributesAndroidInjector
    abstract UserHomeFragment provideUserHomeFragment();


    @ContributesAndroidInjector
    abstract UserSettingsFragment provideUserSettingsFragment();


    @ContributesAndroidInjector
    abstract MeetingsFragment provideMeetingsFragment();


}
