package com.appypie.video.app.ui.joinMeeting

import android.os.Bundle
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseActivity
import com.appypie.video.app.util.CommonMethod

class JoinMeetingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_meeting)


        if (savedInstanceState == null)
            CommonMethod.addFragment(this, JoinMeetingFragment(R.id.joinMeetingContainer), R.id.joinMeetingContainer)

    }
}
