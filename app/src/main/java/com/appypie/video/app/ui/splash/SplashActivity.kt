package com.appypie.video.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseActivity
import com.appypie.video.app.ui.intro.IntroActivity
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.CommonMethod.Companion.isValidToken
import com.appypie.video.app.util.Constants
import com.appypie.video.app.util.Constants.DEVICE_ID
import com.appypie.video.app.util.Constants.DEVICE_TOKEN
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.splash.*
import javax.inject.Inject

class SplashActivity : BaseActivity() {

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null

    private var viewModel: AuthTokenViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.splash)

        viewModel = ViewModelProvider(this, viewModelFactory!!).get(AuthTokenViewModel::class.java)


        if (CommonMethod.isOnline(this)) getAuthToken() else CommonMethod.showFinishAlert(getString(R.string.no_internet), this)

        img.setOnClickListener {
            // makeSplash()
        }

        getDeviceTokens()

    }

    @SuppressLint("HardwareIds")
    private fun getDeviceTokens() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("SplashActivity", "getInstanceId failed" + task.exception)
                        return@OnCompleteListener
                    }
                    DEVICE_TOKEN = task.result?.token
                    Log.e("DEVICE_TOKEN : ", DEVICE_TOKEN)
                })

        DEVICE_ID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


    }


    private fun makeSplash() {
        val handler = Handler()
        handler.postDelayed({ startIntent(IntroActivity::class.java) }, 1500)
    }


    private fun startIntent(cls: Class<*>) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }


    private fun getAuthToken() {
        when {
            AppPrefs.getString(Constants.ACCESS_TOKEN).isEmpty() -> {
                viewModel!!.getAuthToken(CommonMethod.getTokenHeaderMap(), Constants.PASSWORD_GRANT_TYPE, Constants.AUTH_TOKEN_USER, Constants.AUTH_TOKEN_PASSWORD)
            }
            isValidToken(AppPrefs.getString(Constants.REFRESH_TOKEN_EXPIRE_TIME)) -> {
                viewModel!!.refreshToken(CommonMethod.getTokenHeaderMap(), Constants.REFRESH_GRANT_TYPE, AppPrefs.getString(Constants.REFRESH_TOKEN))
            }
            else -> {
                viewModel!!.getAuthToken(CommonMethod.getTokenHeaderMap(), Constants.PASSWORD_GRANT_TYPE, Constants.AUTH_TOKEN_USER, Constants.AUTH_TOKEN_PASSWORD)
            }
        }

        viewModel!!.getResponse().observe(this, Observer { response: AuthTokenResponse? ->
            if (response != null) {
                if (response.statusMsg.toString() == Constants.API_SUCCESS_MSG) {
                    AppPrefs.putString(Constants.ACCESS_TOKEN, response.accessToken)
                    AppPrefs.putString(Constants.REFRESH_TOKEN, response.refreshToken)
                    AppPrefs.putString(Constants.ACCESS_TOKEN_EXPIRE_TIME, response.accessTokenExpiresAt)
                    AppPrefs.putString(Constants.REFRESH_TOKEN_EXPIRE_TIME, response.refreshTokenExpiresAt)
                    startIntent(IntroActivity::class.java)
                } else {
                    viewModel!!.getAuthToken(CommonMethod.getTokenHeaderMap(), Constants.PASSWORD_GRANT_TYPE, Constants.AUTH_TOKEN_USER, Constants.AUTH_TOKEN_PASSWORD)
                }
            }
        })


        viewModel!!.error.observe(this, Observer { isError: Boolean? ->
            if (isError != null) if (isError) {
                CommonMethod.showFinishAlert(Constants.SERVER_ERROR, this)
            }
        })
    }

    override fun onStop() {
        finish()
        super.onStop()
    }

}
