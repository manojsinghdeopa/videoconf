package com.appypie.video.app.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.appypie.video.app.base.BaseApplication;

import static com.appypie.video.app.util.Constants.isInternetAudioEnable;
import static com.appypie.video.app.util.Constants.meetingData;


public class AppPrefs {

    private static SharedPreferences sharedPrefs;

    //    private static Editor edit;
    private static void getInstance() {
        if (sharedPrefs == null) {
            String PREF_NAME = "digital_pref";
            sharedPrefs = BaseApplication.appContext.getSharedPreferences(PREF_NAME, 0);
        }
    }

    public static String putString(String key, String value) {
        getInstance();
        Editor edit = sharedPrefs.edit();
        edit.putString(key, value);
        edit.apply();
        return key;
    }

    public static String putBoolean(String key, Boolean value) {
        getInstance();
        Editor edit = sharedPrefs.edit();
        edit.putBoolean(key, value);
        edit.apply();
        return key;
    }

    public static String getString(String key) {
        getInstance();
        return sharedPrefs.getString(key, "");
    }

    public static Boolean getBoolean(String key) {
        getInstance();
        return sharedPrefs.getBoolean(key, false);
    }

    public static void clearAllPref() {

        String token = getString(Constants.ACCESS_TOKEN);
        String refreshToken = getString(Constants.REFRESH_TOKEN);
        String videoPreview = getString(Constants.SHOW_VIDEO_PREVIEW);

        Editor edit = sharedPrefs.edit();
        edit.clear();
        edit.apply();
        meetingData = null;
        isInternetAudioEnable = false;
        // personalMeetingData = null;

        putString(Constants.ACCESS_TOKEN, token);
        putString(Constants.REFRESH_TOKEN, refreshToken);
        putString(Constants.SHOW_VIDEO_PREVIEW, videoPreview);

    }
}
