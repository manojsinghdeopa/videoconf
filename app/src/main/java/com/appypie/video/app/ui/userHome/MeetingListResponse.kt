package com.appypie.video.app.ui.userHome

import com.appypie.video.app.ui.common.MeetingData
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MeetingListResponse {

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
    var data: Data? = null

    inner class Data {
        @SerializedName("today")
        @Expose
        var today: List<MeetingData>? = null

        @SerializedName("tomorrow")
        @Expose
        var tomorrow: List<MeetingData>? = null

    }
}