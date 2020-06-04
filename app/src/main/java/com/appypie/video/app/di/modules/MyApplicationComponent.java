/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appypie.video.app.di.modules;

import com.appypie.video.app.ui.room.CommunityRoomManagerModule;
import com.appypie.video.app.ui.room.RoomActivityModule;
import com.appypie.video.app.ui.room.VideoServiceModule;
import com.appypie.video.app.base.BaseApplication;
import com.appypie.video.app.di.diUtils.ApplicationScope;
import com.appypie.video.app.ui.room.CommunityRoomManagerModule;
import com.appypie.video.app.ui.room.RoomActivityModule;
import com.appypie.video.app.ui.room.VideoServiceModule;
import com.appypie.video.app.webservices.ApiServiceInterface;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@ApplicationScope
@Component(
        modules = {
                ApplicationContextModule.class,
                ApiServiceModule.class,
                AndroidInjectionModule.class,
                CommunityTreeModule.class,
                AndroidSupportInjectionModule.class,

                ActivityBindingModule.class,

                /* SplashActivityModule.class,*/
                RoomActivityModule.class,
                /* SettingsActivityModule.class,*/
                /*SettingsFragmentModule.class,*/
                CommunityRoomManagerModule.class,
                VideoServiceModule.class,
                AudioSwitchModule.class
        }
)
public interface MyApplicationComponent {

    public ApiServiceInterface getApiInterface();

    void inject(BaseApplication application);


}
