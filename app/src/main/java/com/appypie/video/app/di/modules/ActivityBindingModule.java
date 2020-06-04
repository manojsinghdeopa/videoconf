package com.appypie.video.app.di.modules;

import com.appypie.video.app.ui.common.ContainerActivity;
import com.appypie.video.app.ui.common.ContainerActivityBindingModule;
import com.appypie.video.app.ui.home.HomeActivity;
import com.appypie.video.app.ui.home.HomeActivityBindingModule;
import com.appypie.video.app.ui.intro.IntroActivity;
import com.appypie.video.app.ui.login.LoginActivity;
import com.appypie.video.app.ui.login.LoginBindingModule;
import com.appypie.video.app.ui.splash.SplashActivity;
import com.appypie.video.app.ui.userHome.UserHomeActivity;
import com.appypie.video.app.ui.userHome.UserHomeActivityBindingModule;
import com.appypie.video.app.ui.addEditMeeting.MeetingContainerActivity;
import com.appypie.video.app.ui.addEditMeeting.MeetingContainerActivityBindingModule;
import com.appypie.video.app.ui.common.ContainerActivity;
import com.appypie.video.app.ui.common.ContainerActivityBindingModule;
import com.appypie.video.app.ui.home.HomeActivity;
import com.appypie.video.app.ui.home.HomeActivityBindingModule;
import com.appypie.video.app.ui.intro.IntroActivity;
import com.appypie.video.app.ui.joinMeeting.JoinMeetingActivity;
import com.appypie.video.app.ui.joinMeeting.JoinMeetingBindingModule;
import com.appypie.video.app.ui.login.LoginActivity;
import com.appypie.video.app.ui.login.LoginBindingModule;
import com.appypie.video.app.ui.splash.SplashActivity;
import com.appypie.video.app.ui.userHome.UserHomeActivity;
import com.appypie.video.app.ui.userHome.UserHomeActivityBindingModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {


    @ContributesAndroidInjector
    abstract SplashActivity bindSplashActivity();

    @ContributesAndroidInjector(modules = {LoginBindingModule.class})
    abstract LoginActivity bindLoginActivity();


    @ContributesAndroidInjector
    abstract IntroActivity bindIntroActivity();


    @ContributesAndroidInjector(modules = {JoinMeetingBindingModule.class})
    abstract JoinMeetingActivity bindJoinMeetingActivity();


    @ContributesAndroidInjector(modules = {HomeActivityBindingModule.class})
    abstract HomeActivity bindHomeActivity();

    @ContributesAndroidInjector(modules = {UserHomeActivityBindingModule.class})
    abstract UserHomeActivity bindUserHomeActivity();


    @ContributesAndroidInjector(modules = {MeetingContainerActivityBindingModule.class})
    abstract MeetingContainerActivity bindMeetingContainerActivity();


    @ContributesAndroidInjector(modules = {ContainerActivityBindingModule.class})
    abstract ContainerActivity bindContainerActivity();


}
