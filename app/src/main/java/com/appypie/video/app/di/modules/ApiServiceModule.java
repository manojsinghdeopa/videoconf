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

import android.app.Application;
import android.content.SharedPreferences;

import com.appypie.video.app.di.diUtils.ApplicationScope;
import com.appypie.video.app.webservices.ApiServiceInterface;
import com.appypie.video.app.webservices.TokenService;
import com.appypie.video.app.webservices.VideoAppService;
import com.appypie.video.app.webservices.VideoAppServiceDelegate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import timber.log.Timber;


@Module(includes = ViewModelModule.class)
public class ApiServiceModule {

    private static final String VIDEO_APP_SERVICE_DEV_URL = "https://app.dev.video.bytwilio.com";
    private static final String VIDEO_APP_SERVICE_STAGE_URL = "https://app.stage.video.bytwilio.com";
    private static final String VIDEO_APP_SERVICE_PROD_URL = "https://app.video.bytwilio.com";

    private static final String BASE_URL = "https://videoconfer.pbodev.info";


    @Provides
    @ApplicationScope
    ApiServiceInterface getApiInterface(Retrofit retroFit) {
        return retroFit.create(ApiServiceInterface.class);
    }


    @Provides
    @ApplicationScope
    Cache provideHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024;
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @ApplicationScope
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient();
        return gsonBuilder.create();
    }

    @Provides
    @ApplicationScope
    OkHttpClient provideOkhttpClient(Cache cache) {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(/*message -> Timber.i(message)*/);
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.cache(cache);
        client.interceptors().add(httpLoggingInterceptor);
        return client.build();
    }

    @Provides
    @ApplicationScope
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();
    }


    @Provides
    @ApplicationScope
    @Named("VideoAppService")
    OkHttpClient providesOkHttpClient() {
        return new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor()).build();
    }

    @Provides
    @ApplicationScope
    @Named("VideoAppServiceDev")
    VideoAppService providesVideoAppServiceDev(@Named("VideoAppService") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_DEV_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VideoAppService.class);
    }

    @Provides
    @ApplicationScope
    @Named("VideoAppServiceStage")
    VideoAppService providesVideoAppServiceStage(@Named("VideoAppService") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_STAGE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VideoAppService.class);
    }

    @Provides
    @ApplicationScope
    @Named("VideoAppServiceProd")
    VideoAppService providesVideoAppServiceProd(@Named("VideoAppService") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_PROD_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VideoAppService.class);
    }

    @Provides
    @ApplicationScope
    VideoAppServiceDelegate providesVideoAppServiceDelegate(
            SharedPreferences sharedPreferences,
            @Named("VideoAppServiceDev") VideoAppService videoAppServiceDev,
            @Named("VideoAppServiceStage") VideoAppService videoAppServiceStage,
            @Named("VideoAppServiceProd") VideoAppService videoAppServiceProd) {

        return new VideoAppServiceDelegate(
                sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd);
    }

    @Provides
    @ApplicationScope
    TokenService providesTokenService(final VideoAppServiceDelegate videoAppServiceDelegate) {
        return videoAppServiceDelegate;
    }
}
