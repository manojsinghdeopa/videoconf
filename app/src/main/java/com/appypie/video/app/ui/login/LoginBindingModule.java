package com.appypie.video.app.ui.login;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class LoginBindingModule {

    @ContributesAndroidInjector
    abstract LoginFragment provideListFragment();

}
