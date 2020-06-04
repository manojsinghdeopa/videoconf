package com.appypie.video.app.ui.joinMeeting;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class JoinMeetingBindingModule {

    @ContributesAndroidInjector
    abstract JoinMeetingFragment provideJoinMeetingFragment();

    @ContributesAndroidInjector
    abstract AfterJoinMeeting provideAfterJoinMeeting();

    @ContributesAndroidInjector
    abstract VideoPreviewFragment provideVideoPreviewFragment();

}
