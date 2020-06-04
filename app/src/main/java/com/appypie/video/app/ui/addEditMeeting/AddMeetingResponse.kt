package com.appypie.video.app.ui.addEditMeeting

import com.appypie.video.app.ui.common.MeetingData
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddMeetingResponse {

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("status_msg")
    @Expose
    var statusMsg: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("meeting_data")
    @Expose
    var meetingData: MeetingData? = null


}