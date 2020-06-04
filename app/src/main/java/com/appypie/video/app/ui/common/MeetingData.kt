package com.appypie.video.app.ui.common

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MeetingData {

    @SerializedName("password_enabled")
    @Expose
    var passwordEnabled: Boolean? = null

    @SerializedName("enable_waiting_room")
    @Expose
    var enableWaitingRoom: Boolean? = null

    @SerializedName("video_host")
    @Expose
    var videoHost: Boolean? = null

    @SerializedName("video_participant")
    @Expose
    var videoParticipant: Boolean? = null

    @SerializedName("audio_host")
    @Expose
    var audioHost: Boolean? = null

    @SerializedName("waiting_room")
    @Expose
    var waitingRoom: Boolean? = null

    @SerializedName("allow_join_before")
    @Expose
    var allowJoinBefore: Boolean? = null

    @SerializedName("automatic_recording")
    @Expose
    var automaticRecording: Boolean? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("appId")
    @Expose
    var appId: String? = null

    @SerializedName("topic")
    @Expose
    var topic: String? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("start_date")
    @Expose
    var startDate: String? = null

    @SerializedName("start_time")
    @Expose
    var startTime: String? = null

    @SerializedName("duration")
    @Expose
    var duration: Int? = null

    @SerializedName("time_zone")
    @Expose
    var timeZone: String? = null

    @SerializedName("meeting_type")
    @Expose
    var meetingType: String? = null

    @SerializedName("host_id")
    @Expose
    var hostId: String? = null

    @SerializedName("host_name")
    @Expose
    var hostName: String? = null

    @SerializedName("host_email")
    @Expose
    var hostEmail: String? = null

    @SerializedName("creater_id")
    @Expose
    var createrId: String? = null

    @SerializedName("creater_name")
    @Expose
    var createrName: String? = null

    @SerializedName("creater_email")
    @Expose
    var createrEmail: String? = null

    @SerializedName("created_by")
    @Expose
    var createdBy: String? = null

    @SerializedName("added_date")
    @Expose
    var addedDate: String? = null

    @SerializedName("server_start_datetime")
    @Expose
    var serverStartDatetime: String? = null

    @SerializedName("server_start_timestamp")
    @Expose
    var serverStartTimestamp: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("meeting_password")
    @Expose
    var meetingPassword: Any? = null

    @SerializedName("meeting_md5password")
    @Expose
    var meetingMd5password: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null

    @SerializedName("meeting_id")
    @Expose
    var meetingId: String? = null

    @SerializedName("meeting_link")
    @Expose
    var meetingLink: String? = null

    @SerializedName("meeting_server_time")
    @Expose
    var meetingServerTime: Int? = null

    @SerializedName("current_started_id")
    @Expose
    var currentStartedId: Int? = null

    @SerializedName("ended_date")
    @Expose
    var endedDate: String? = null

    @SerializedName("started_date")
    @Expose
    var startedDate: String? = null

}