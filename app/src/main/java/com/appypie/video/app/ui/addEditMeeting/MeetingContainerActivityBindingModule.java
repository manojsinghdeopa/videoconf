package com.appypie.video.app.ui.addEditMeeting;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MeetingContainerActivityBindingModule {


    @ContributesAndroidInjector
    abstract AddMeetingFragment provideAddMeetingFragment();


    @ContributesAndroidInjector
    abstract MeetingDetailFragment provideMeetingDetailFragment();


    @ContributesAndroidInjector
    abstract StartMeetingFragment provideStartMeetingFragment();


}
