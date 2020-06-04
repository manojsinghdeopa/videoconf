package com.appypie.video.app.di.modules;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.appypie.video.app.ViewModelFactory;
import com.appypie.video.app.di.diUtils.ViewModelKey;
import com.appypie.video.app.ui.addEditMeeting.AddMeetingViewModel;
import com.appypie.video.app.ui.addEditMeeting.StartMeetingViewModel;
import com.appypie.video.app.ui.joinMeeting.JoinMeetingViewModel;
import com.appypie.video.app.ui.login.LoginViewModel;
import com.appypie.video.app.ui.room.EndMeetingViewModel;
import com.appypie.video.app.ui.splash.AuthTokenViewModel;
import com.appypie.video.app.ui.userHome.CMSDataViewModel;
import com.appypie.video.app.ui.userHome.DeleteMeetingViewModel;
import com.appypie.video.app.ui.userHome.MeetingListViewModel;
import com.appypie.video.app.ui.userHome.PersonalMeetingViewModel;
import com.appypie.video.app.ui.userHome.ResumeMeetingViewModel;
import com.appypie.video.app.ui.userHome.UpcomingMeetingViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;


@Module
public abstract class ViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(AuthTokenViewModel.class)
    abstract ViewModel bindAuthTokenViewModel(AuthTokenViewModel authTokenViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    abstract ViewModel bindLoginViewModel(LoginViewModel loginViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(JoinMeetingViewModel.class)
    abstract ViewModel bindJoinMeetingViewModel(JoinMeetingViewModel joinMeetingViewModel);


    @Binds
    @IntoMap
    @ViewModelKey(PersonalMeetingViewModel.class)
    abstract ViewModel bindPersonalMeetingViewModel(PersonalMeetingViewModel personalMeetingViewModel);


    @Binds
    @IntoMap
    @ViewModelKey(AddMeetingViewModel.class)
    abstract ViewModel bindAddMeetingViewModel(AddMeetingViewModel addMeetingViewModel);


    @Binds
    @IntoMap
    @ViewModelKey(UpcomingMeetingViewModel.class)
    abstract ViewModel bindUpcomingMeetingViewModel(UpcomingMeetingViewModel upcomingMeetingViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CMSDataViewModel.class)
    abstract ViewModel bindCMSDataViewModel(CMSDataViewModel cmsDataViewModel);


    @Binds
    @IntoMap
    @ViewModelKey(StartMeetingViewModel.class)
    abstract ViewModel bindStartMeetingViewModel(StartMeetingViewModel startMeetingViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MeetingListViewModel.class)
    abstract ViewModel bindMeetingListViewModel(MeetingListViewModel meetingListViewModel);


    @Binds
    @IntoMap
    @ViewModelKey(DeleteMeetingViewModel.class)
    abstract ViewModel bindDeleteMeetingViewModel(DeleteMeetingViewModel deleteMeetingViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ResumeMeetingViewModel.class)
    abstract ViewModel bindResumeMeetingViewModel(ResumeMeetingViewModel resumeMeetingViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EndMeetingViewModel.class)
    abstract ViewModel bindEndMeetingViewModel(EndMeetingViewModel endMeetingViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);


}
