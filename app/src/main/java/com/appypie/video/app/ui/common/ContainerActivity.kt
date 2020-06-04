package com.appypie.video.app.ui.common

import android.os.Bundle
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseActivity
import com.appypie.video.app.ui.addEditMeeting.StartMeetingFragment
import com.appypie.video.app.ui.joinMeeting.AfterJoinMeeting
import com.appypie.video.app.util.CommonMethod
import com.appypie.video.app.util.Constants


class ContainerActivity : BaseActivity() {


    private var savedInstanceState: Bundle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_container)


        if (intent.getStringExtra(Constants.TITLE) == getString(R.string.start_meeting)) {
            CommonMethod.addFragment(this, StartMeetingFragment(), R.id.commonContainer)
        } else {
            CommonMethod.addFragment(this, AfterJoinMeeting(R.id.commonContainer), R.id.commonContainer)
        }

    }


}
