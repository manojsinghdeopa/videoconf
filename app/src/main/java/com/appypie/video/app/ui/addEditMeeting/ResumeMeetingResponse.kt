package com.appypie.video.app.ui.addEditMeeting

import com.appypie.video.app.ui.common.MeetingData
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ResumeMeetingResponse {
    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("status_msg")
    @Expose
    var statusMsg: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: MeetingData? = null

    @SerializedName("token")
    @Expose
    var token: String? = null


}