package com.appypie.video.app.di.modules

import android.app.Application
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.appypie.video.app.di.modules.ApplicationContextModule
import dagger.Module
import dagger.Provides

@Module(includes = [ApplicationContextModule::class])
class AudioSwitchModule {

    @Provides
    fun providesAudioDeviceSelector(application: Application): AudioDeviceSelector =
            AudioDeviceSelector(application)
}