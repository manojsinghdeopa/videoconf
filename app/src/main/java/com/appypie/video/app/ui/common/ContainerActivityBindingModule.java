package com.appypie.video.app.ui.common;

import com.appypie.video.app.ui.addEditMeeting.StartMeetingFragment;
import com.appypie.video.app.ui.joinMeeting.AfterJoinMeeting;
import com.appypie.video.app.ui.joinMeeting.VideoPreviewFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ContainerActivityBindingModule {


    @ContributesAndroidInjector
    abstract AfterJoinMeeting provideAfterJoinMeeting();

    @ContributesAndroidInjector
    abstract VideoPreviewFragment provideVideoPreviewFragment();


    @ContributesAndroidInjector
    abstract StartMeetingFragment provideStartMeetingFragment();


}
