package com.appypie.video.app.ui.addEditMeeting

import android.os.Bundle
import com.appypie.video.app.R
import com.appypie.video.app.base.BaseActivity
import com.appypie.video.app.util.CommonMethod.Companion.addFragment
import com.appypie.video.app.util.Constants


class MeetingContainerActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_container)

        if (intent.extras != null) {
            if (intent.getStringExtra(Constants.FROM) == "MeetingList") {
                addFragment(this, MeetingDetailFragment(), R.id.meetingContainer)
            }
        } else {
            addFragment(this, AddMeetingFragment(), R.id.meetingContainer)
        }
    }


}
