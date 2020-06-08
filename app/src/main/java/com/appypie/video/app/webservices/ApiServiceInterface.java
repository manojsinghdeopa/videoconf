package com.appypie.video.app.webservices;

import com.appypie.video.app.ui.addEditMeeting.AddMeetingResponse;
import com.appypie.video.app.ui.addEditMeeting.ResumeMeetingResponse;
import com.appypie.video.app.ui.joinMeeting.JoinMeetingResponse;
import com.appypie.video.app.ui.splash.AuthTokenResponse;
import com.appypie.video.app.ui.userHome.CmsDataResponse;
import com.appypie.video.app.ui.userHome.MeetingDateListResponse;
import com.appypie.video.app.ui.userHome.MeetingListResponse;
import com.appypie.video.app.ui.userHome.PersonalMeetingResponse;
import com.google.gson.JsonObject;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiServiceInterface {


    @Headers("Accept: */*")
    @GET("backend/token")
    Call<String> getToken(@Query("identity") String identity,
                          @Query("roomName") String roomName);


    @Headers("Accept: */*")
    @GET("backend/token")
    Single<String> getTokens(@Query("identity") String identity,
                             @Query("roomName") String roomName);


    @FormUrlEncoded
    @POST("backend/oauth/token")
    Call<JsonObject> postToken(@HeaderMap Map<String, String> headers,
                               @Field("grant_type") String grant_type,
                               @Field("username") String username,
                               @Field("password") String password);


    @FormUrlEncoded
    @POST("backend/oauth/token")
    Single<AuthTokenResponse> getAuthToken(@HeaderMap Map<String, String> headers,
                                           @Field("grant_type") String grant_type,
                                           @Field("username") String username,
                                           @Field("password") String password);


    @FormUrlEncoded
    @POST("backend/oauth/token")
    Single<AuthTokenResponse> refreshToken(@HeaderMap Map<String, String> headers,
                                           @Field("grant_type") String grant_type,
                                           @Field("refresh_token") String username);


    @FormUrlEncoded
    @POST("backend/api/meetings/addmeeting")
    Single<AddMeetingResponse> addMeeting(@HeaderMap Map<String, String> headers,
                                          @Field("app_id") String app_id,
                                          @Field("topic") String topic,
                                          @Field("description") String description,
                                          @Field("start_date") String start_date,
                                          @Field("start_time") String start_time,

                                          @Field("time_zone") String time_zone,
                                          @Field("password_enabled") String password_enabled,
                                          @Field("video_host") String video_host,
                                          @Field("video_participant") String video_participant,
                                          @Field("audio_host") String audio_host,

                                          @Field("duration") String duration,
                                          @Field("email") String email,
                                          @Field("meeting_password") String meeting_password,
                                          @Field("host_id") String host_id,
                                          @Field("host_name") String host_name,
                                          @Field("host_email") String host_email,
                                          @Field("created_by") String created_by);


    @FormUrlEncoded
    @POST("backend/api/meetings/editmeeting")
    Single<AddMeetingResponse> editMeeting(@HeaderMap Map<String, String> headers,
                                           @Field("app_id") String app_id,
                                           @Field("topic") String topic,
                                           @Field("description") String description,
                                           @Field("start_date") String start_date,
                                           @Field("start_time") String start_time,

                                           @Field("time_zone") String time_zone,
                                           @Field("password_enabled") String password_enabled,
                                           @Field("video_host") String video_host,
                                           @Field("video_participant") String video_participant,
                                           @Field("audio_host") String audio_host,
                                           @Field("duration") String duration,

                                           @Field("meeting_password") String meeting_password,
                                           @Field("meeting_id") String meeting_id,
                                           @Field("host_id") String host_id);

    @FormUrlEncoded
    @POST("backend/api/meetings/joinmeeting")
    Single<JoinMeetingResponse> joinMeeting(@HeaderMap Map<String, String> headers,
                                            @Field("meeting_id") String meeting_id,
                                            @Field("username") String username,
                                            @Field("email") String email,
                                            @Field("password") String password,
                                            @Field("app_id") String app_id,
                                            @Field("device_type") String device_type,
                                            @Field("device_token") String device_token,
                                            @Field("device_id") String device_id);


    @FormUrlEncoded
    @POST("backend/api/meetings/startmeeting")
    Single<JoinMeetingResponse> startMeeting(@HeaderMap Map<String, String> headers,
                                             @Field("app_id") String app_id,
                                             @Field("meeting_id") String meeting_id,
                                             @Field("username") String username,
                                             @Field("host_id") String host_id,
                                             @Field("password") String password,
                                             @Field("device_type") String device_type,
                                             @Field("device_token") String device_token,
                                             @Field("device_id") String device_id);


    @FormUrlEncoded
    @POST("backend/api/meetings/personalmeeting")
    Single<PersonalMeetingResponse> getPersonalMeeting(@HeaderMap Map<String, String> headers,
                                                       @Field("app_id") String app_id,
                                                       @Field("host_name") String host_name,
                                                       @Field("host_email") String host_email,
                                                       @Field("host_id") String host_id);


    @FormUrlEncoded
    @POST("backend/api/meetings/meetingdatelist")
    Single<MeetingDateListResponse> getMeetingDateList(@HeaderMap Map<String, String> headers,
                                                       @Field("app_id") String app_id,
                                                       @Field("host_id") String host_id,
                                                       @Field("custom_date") String custom_date,
                                                       @Field("zone") String zone);


    @FormUrlEncoded
    @POST("backend/api/meetings/meetingdashboardlist")
    Single<MeetingDateListResponse> getDashboardMeetingList(@HeaderMap Map<String, String> headers,
                                                            @Field("app_id") String app_id,
                                                            @Field("host_id") String host_id,
                                                            @Field("start_date") String start_date,
                                                            @Field("end_date") String end_date,
                                                            @Field("zone") String zone);


    @FormUrlEncoded
    @POST("backend/api/meetings/meetinglist")
    Single<MeetingListResponse> getMeetingList(@HeaderMap Map<String, String> headers,
                                               @Field("app_id") String app_id,
                                               @Field("host_id") String host_id,
                                               @Field("time_zone") String time_zone);

    @FormUrlEncoded
    @POST("backend/api/meetings/cmsdata")
    Single<CmsDataResponse> getCmsData(@HeaderMap Map<String, String> headers,
                                       @Field("app_id") String app_id,
                                       @Field("identifire") String identifire);


    @FormUrlEncoded
    @POST("backend/api/meetings/deletemeeting")
    Single<CommonResponse> deleteMeeting(@HeaderMap Map<String, String> headers,
                                         @Field("app_id") String app_id,
                                         @Field("host_id") String host_id,
                                         @Field("meeting_id") String meeting_id);


    @FormUrlEncoded
    @POST("backend/api/meetings/endmeeting")
    Single<CommonResponse> endMeeting(@HeaderMap Map<String, String> headers,
                                      @Field("app_id") String app_id,
                                      @Field("host_id") String host_id,
                                      @Field("meeting_id") String meeting_id);


    @FormUrlEncoded
    @POST("backend/api/meetings/leftmeeting")
    Single<CommonResponse> leftMeeting(@HeaderMap Map<String, String> headers,
                                       @Field("app_id") String app_id,
                                       @Field("username") String username,
                                       @Field("meeting_id") String meeting_id);


    @FormUrlEncoded
    @POST("backend/api/meetings/resumemeeting")
    Single<ResumeMeetingResponse> resumeMeeting(@HeaderMap Map<String, String> headers,
                                                @Field("app_id") String app_id,
                                                @Field("username") String username,
                                                @Field("email") String email,
                                                @Field("password") String password,
                                                @Field("meeting_id") String meeting_id);


}
