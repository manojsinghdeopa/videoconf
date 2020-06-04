package com.appypie.video.app.ui.home;

import com.appypie.video.app.ui.room.ParticipantFragment;
import com.appypie.video.app.ui.room.RoomFragment;
import com.appypie.video.app.ui.room.ParticipantFragment;
import com.appypie.video.app.ui.room.RoomFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class HomeActivityBindingModule {


    @ContributesAndroidInjector
    abstract RoomFragment provideRoomFragment();


    @ContributesAndroidInjector
    abstract ParticipantFragment provideParticipantFragment();


    @ContributesAndroidInjector
    abstract VideoRoomFragment provideVideoRoomFragment();


}
