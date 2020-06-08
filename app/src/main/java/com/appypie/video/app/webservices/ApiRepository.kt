package com.appypie.video.app.webservices

import com.appypie.video.app.ui.addEditMeeting.AddMeetingResponse
import com.appypie.video.app.ui.addEditMeeting.ResumeMeetingResponse
import com.appypie.video.app.ui.joinMeeting.JoinMeetingResponse
import com.appypie.video.app.ui.splash.AuthTokenResponse
import com.appypie.video.app.ui.userHome.CmsDataResponse
import com.appypie.video.app.ui.userHome.MeetingDateListResponse
import com.appypie.video.app.ui.userHome.MeetingListResponse
import com.appypie.video.app.ui.userHome.PersonalMeetingResponse
import com.appypie.video.app.util.Constants
import io.reactivex.Single
import javax.inject.Inject

class ApiRepository @Inject constructor(private val repoService: ApiServiceInterface) {

    fun getRepositories(identity: String?, room_name: String?): Single<String> {
        return repoService.getTokens(identity, room_name)
    }

    fun getAuthToken(headers: Map<String, String>, grant_type: String?, username: String?, password: String?): Single<AuthTokenResponse> {
        return repoService.getAuthToken(headers, grant_type, username, password)
    }


    fun refreshToken(headers: Map<String, String>, grant_type: String?, refresh_token: String?): Single<AuthTokenResponse> {
        return repoService.refreshToken(headers, grant_type, refresh_token)
    }

    fun joinMeeting(headers: Map<String, String>, meeting_id: String?, username: String?, email: String?, password: String?, app_id: String?): Single<JoinMeetingResponse> {
        return repoService.joinMeeting(headers, meeting_id, username, email, password, app_id, Constants.DEVICE_TYPE, Constants.DEVICE_TOKEN, Constants.DEVICE_ID)
    }

    fun getPersonalMeeting(headers: Map<String, String>, app_id: String?, host_name: String?, host_email: String?, host_id: String?): Single<PersonalMeetingResponse> {
        return repoService.getPersonalMeeting(headers, app_id, host_name, host_email, host_id)
    }

    fun addMeeting(headers: Map<String, String>,
                   app_id: String?, topic: String?, description: String?,
                   start_date: String?, start_time: String?, time_zone: String?,
                   password_enabled: String?, video_host: String?, video_participant: String?,
                   audio_host: String?, duration: String?, email: String?,
                   meeting_password: String?, host_id: String?, host_name: String?,
                   host_email: String?, created_by: String?): Single<AddMeetingResponse> {
        return repoService.addMeeting(headers, app_id, topic, description,
                start_date, start_time, time_zone, password_enabled, video_host,
                video_participant, audio_host, duration, email, meeting_password,
                host_id, host_name, host_email, created_by)
    }


    fun editMeeting(headers: Map<String, String>,
                    app_id: String?, topic: String?, description: String?,
                    start_date: String?, start_time: String?, time_zone: String?,
                    password_enabled: String?, video_host: String?, video_participant: String?,
                    audio_host: String?, duration: String?,
                    meeting_password: String?, meeting_id: String?, host_id: String?): Single<AddMeetingResponse> {
        return repoService.editMeeting(headers, app_id, topic, description,
                start_date, start_time, time_zone, password_enabled,
                video_host, video_participant, audio_host, duration,
                meeting_password, meeting_id, host_id)
    }

    fun deleteMeeting(headers: Map<String, String>, app_id: String?, host_id: String?, meeting_id: String?): Single<CommonResponse> {
        return repoService.deleteMeeting(headers, app_id, host_id, meeting_id)
    }

    fun startMeeting(headers: Map<String, String>,
                     app_id: String?, meeting_id: String?, username: String?,
                     host_id: String?, password: String?): Single<JoinMeetingResponse> {
        return repoService.startMeeting(headers, app_id, meeting_id, username, host_id, password, Constants.DEVICE_TYPE, Constants.DEVICE_TOKEN, Constants.DEVICE_ID)
    }

    fun getMeetingDateList(headers: Map<String, String>,
                           app_id: String?, host_id: String?, custom_date: String?,
                           zone: String?): Single<MeetingDateListResponse> {
        return repoService.getMeetingDateList(headers, app_id, host_id, custom_date, zone)
    }


    fun getDashboardMeetingList(headers: Map<String, String>,
                                app_id: String?, host_id: String?,
                                start_date: String?, end_date: String?,
                                zone: String?): Single<MeetingDateListResponse> {
        return repoService.getDashboardMeetingList(headers, app_id, host_id, start_date, end_date, zone)
    }

    fun getMeetingList(headers: Map<String, String>,
                       app_id: String?, host_id: String?, time_zone: String?): Single<MeetingListResponse> {
        return repoService.getMeetingList(headers, app_id, host_id, time_zone)
    }

    fun getCmsData(headers: Map<String, String>, app_id: String?, identifire: String?): Single<CmsDataResponse> {
        return repoService.getCmsData(headers, app_id, identifire)
    }


    fun resumeMeeting(headers: Map<String, String>, app_id: String?, username: String?, email: String?, password: String?, meeting_id: String?): Single<ResumeMeetingResponse> {
        return repoService.resumeMeeting(headers, app_id, username, email, password, meeting_id)
    }

    fun endMeeting(headers: Map<String, String>, app_id: String?, host_id: String?, meeting_id: String?): Single<CommonResponse> {
        return repoService.endMeeting(headers, app_id, host_id, meeting_id)
    }

    fun leftMeeting(headers: Map<String, String>, app_id: String?, username: String?, meeting_id: String?): Single<CommonResponse> {
        return repoService.leftMeeting(headers, app_id, username, meeting_id)
    }


}