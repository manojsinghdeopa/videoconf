package com.appypie.video.app.ui.room

import android.app.Application
import android.content.SharedPreferences
import com.appypie.video.app.data.DataModule
import com.appypie.video.app.di.diUtils.ApplicationScope
import com.appypie.video.app.di.modules.ApiServiceModule
import com.appypie.video.app.di.modules.ApplicationContextModule
import dagger.Module
import dagger.Provides

@Module(includes = [ApplicationContextModule::class, DataModule::class, ApiServiceModule::class])
class RoomManagerModule {

    @Provides
    @ApplicationScope
    fun providesRoomManager(application: Application, sharedPreferences: SharedPreferences): RoomManager {
        return RoomManager(application, sharedPreferences)
    }

}