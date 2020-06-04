package com.appypie.video.app;

import com.appypie.video.app.auth.AuthModule;
import com.appypie.video.app.data.DataModule;
import com.appypie.video.app.data.api.VideoAppServiceModule;
import com.appypie.video.app.ui.ScreenSelectorModule;
import com.appypie.video.app.ui.room.RoomActivityModule;
import com.appypie.video.app.ui.room.RoomManagerModule;
import com.appypie.video.app.ui.room.VideoServiceModule;
import com.appypie.video.app.ui.settings.SettingsActivityModule;
import com.appypie.video.app.ui.settings.SettingsFragmentModule;

import dagger.Component;
import dagger.android.AndroidInjectionModule;

@ApplicationScope
@Component(
    modules = {
        AndroidInjectionModule.class,
        ApplicationModule.class,
        TreeModule.class,
        DataModule.class,
        VideoAppServiceModule.class,
        ScreenSelectorModule.class,
        AuthModule.class,
        SplashActivityModule.class,
        LoginActivityModule.class,
        RoomActivityModule.class,
        SettingsActivityModule.class,
        SettingsFragmentModule.class,
        VideoServiceModule.class,
        RoomManagerModule.class,
        AudioSwitchModule.class
    }
)
public interface VideoApplicationComponent {
    void inject(VideoApplication application);
}
