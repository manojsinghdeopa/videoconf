package com.appypie.video.app.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appypie.video.app.R
import com.appypie.video.app.ViewModelFactory
import com.appypie.video.app.base.BaseActivity
import com.appypie.video.app.ui.joinMeeting.JoinMeetingActivity
import com.appypie.video.app.ui.userHome.PersonalMeetingViewModel
import com.appypie.video.app.ui.userHome.UserHomeActivity
import com.appypie.video.app.util.AppPrefs
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.Constants
import com.appypie.video.app.util.Constants.*
import kotlinx.android.synthetic.main.activity_intro_new.*
import javax.inject.Inject


class IntroActivity : BaseActivity() {

    @JvmField
    @Inject
    var viewModelFactory: ViewModelFactory? = null
    private var viewModel: PersonalMeetingViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_new)
        viewModel = ViewModelProvider(this, viewModelFactory!!).get(PersonalMeetingViewModel::class.java)
        observeViewModel()

        performOnClicks()


    }


    override fun onResume() {
        super.onResume()
        AppPrefs.clearAllPref()
        shouldBindRoom = false
        resumeMeeting = false
    }

    private fun performOnClicks() {

        btnJoinMeeting.setOnClickListener {
            Constants.APP_TYPE = Constants.GUEST
            startActivity(Intent(this, JoinMeetingActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            Constants.APP_TYPE = Constants.USER
            requestUserData()
        }

        btnSignUp.setOnClickListener {
            // startActivity(Intent(this, RoomActivity::class.java))
        }

    }

    private fun requestUserData() {
        viewModel!!.call(CommonMethod.getHeaderMap(), APP_ID, HOST_NAME, HOST_EMAIL, HOST_ID)
    }


    private fun observeViewModel() {

        viewModel!!.response.observe(this, Observer {

            if (it != null) {

                if (this.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (it.status == 200) {

                        PERSONAL_MEETING_ID = it.meetingData?.meetingId
                        PERSONAL_MEETING_LINK = it.meetingData?.meetingLink
                        PERSONAL_MEETING_NAME = it.meetingData?.topic
                        CURRENT_USER_NAME = HOST_NAME
                        CURRENT_USER_EMAIL = HOST_EMAIL

                        personalMeetingData = it.meetingData

                        startActivity(Intent(this, UserHomeActivity::class.java))


                    } else {
                        CommonMethod.showToast(this, it.message!!)
                    }
                }
            }
        })

        viewModel!!.error.observe(this, Observer {
            if (it != null) if (it) {
                CommonMethod.showToast(this, Constants.SERVER_ERROR)
            }
        })


        viewModel!!.loading.observe(this, Observer {
            if (it != null) {
                progressBar!!.visibility = if (it) View.VISIBLE else View.GONE
                // btnSignIn.text = if (it) "Signing..." else "Sign In"
                btnSignIn.isClickable = !it
            }
        })

    }
}
